package foundation.lisp.types;

import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lambda extends TFunction<TObject, TObject>
{
    public static final String EMPTY_PARAM_TERM = "()";
    private static int nextLambdaID;
    /*
        A list of paramAliases for arguments which is defined in the declaration of this lambda term.
     */
    private final List<String> paramAliases;
    private final String funcTerm;
    private final @NotNull Interpreter in;

    public Lambda( final @NotNull Interpreter in, final String lambdaTerm ) throws InvalidTermException
    {
        super("$lambda" + nextID(), lambdaTerm);
        this.in = in;
        final @NotNull List<String> terms = in.splitByTerms(lambdaTerm, true, false);
        if (terms.size() == 3)
        {
            final String paramsTerm = terms.get(1);
            if (paramsTerm.equals(EMPTY_PARAM_TERM))
            {
                this.paramAliases = Collections.emptyList();
            }
            else
            {
                this.paramAliases = Arrays.asList(in.free(terms.get(1), true).split(" "));
            }
            this.funcTerm = terms.get(2);
        }
        else
        {
            throw new InvalidTermException("Malformed lambda term: " + lambdaTerm);
        }
        //
        in.getAtomicFunctions().put(getName(), this);
    }



    private static int nextID()
    {
        return ++nextLambdaID;
    }

    @Override
    String mismatchMessage()
    {
        return "Arity mismatch: lambda, expected " + paramAliases.size() + " arguments";
    }

    @Override
    public boolean argsArityMatch(final int argsCount)
    {
        return argsCount == paramAliases.size();
    }

    @Override
    public String valueToString()
    {
        final int paramsCount = paramAliases.size();
        final String result;
        if (paramsCount == 0)
        {
            result = String.format("(lambda () %s)", funcTerm);
        }
        else
        {
            final String params = paramAliases.stream()
                    .reduce("", (u, v) -> u.concat(" " + v));
            result = String.format("(lambda (%s) %s)", params.substring(1, params.length()), funcTerm);
        }
        //
        return result;
    }

    @Override
    public @NotNull TObject<?> call(final @NotNull List<TObject> args) throws InterpreterException
    {
        // We guarantee that args.size() == paramAliases.size() at this point
        assert args.size() == paramAliases.size() : "Assert: The number of arguments passed to the lambda " + funcTerm + " is not the same as the number of declared arguments";
        // System.out.println("Evaluating lambda: " + funcTerm);
        // System.out.println(paramAliases);
        // System.out.println(args);
        final @NotNull Map<String, String> aliasToArg = new HashMap<>();
        for (int i = 0; i < args.size(); i++)
        {
            aliasToArg.put(paramAliases.get(i), args.get(i).termToString());
        }
        final String rewrittenFuncTerm = in.rewrite(funcTerm, aliasToArg);
        // NOTE: rewrittenFuncTerm не перезаписывается!!!
        return in.eval( rewrittenFuncTerm );
    }

}
