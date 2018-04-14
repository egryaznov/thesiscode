package foundation.lisp.types;

import foundation.lisp.exceptions.InterpreterException;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// NOTE: Value этого класса хранится как строка без одинарных кавычек,
public class TString extends TObject<String>
{
    private static final TString EMPTY_STRING = new TString("", false);

    TString(final String value, final boolean quoted)
    {
        super(Type.STRING);
        setValue( quoted? removeQuotes(value) : value );
    }



    /*
        Removes single quotes from the string `s`.
        For example, removeQuotes("'hello'") = "hello"
     */
    private static @NotNull String removeQuotes(final @NotNull String s)
    {
        final int len = s.length();
        return s.substring(1, len - 1);
    }

    private @NotNull TList chars()
    {
        assert getValue() != null : "Assert TString.chars, value is null!";
        final @NotNull var chars = new LinkedList<TObject<?>>();
        for (int i = 0; i < getValue().length(); i++)
        {
            chars.add(this.substring(i, i + 1));
        }
        //
        return new TList(chars);
    }

    private @NotNull TString substring(final int beginIndexInclusive, final int endIndexExclusive)
    {
        assert getValue() != null : "Assert TString.substring, value is null!";
        return new TString(getValue().substring(beginIndexInclusive, endIndexExclusive), false);
    }

    private @NotNull TString concat(final @NotNull TString s)
    {
        // getValue() != null guaranteed
        assert ( getValue() != null ) : "Assert: TString.concat, getValue() is NULL";
        assert ( s.getValue() != null ) : "Assert: TString.concat, s.getValue is NULL";
        final String s1 = getValue();
        final String s2 = s.getValue();
        return new TString( s1.concat(s2), false );
    }

    static boolean isString(final String term)
    {
        final char firstChar = term.charAt(0);
        final char lastChar = term.charAt(term.length() - 1);
        final char SINGLE_QUOTE = '\'';
        return (firstChar == SINGLE_QUOTE) && (lastChar == SINGLE_QUOTE);
    }

    public static void registerAtomicFunctions(final @NotNull Map<String, TFunction> dict)
    {
        final @NotNull TFunction concatenation = new Concatenation();
        dict.put( concatenation.getName(), concatenation );
        //
        final @NotNull var charsFunc = new Characters();
        dict.put( charsFunc.getName(), charsFunc );
    }

    @Override
    public @NotNull String termToString()
    {
        return "'" + valueToString() + "'";
    }



    private static class Characters extends TFunction<TString, TList>
    {
        private Characters()
        {
            super("chars", "chars");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TString> args) throws InterpreterException
        {
            // args.size == 1
            final @NotNull TString str = args.get(0);
            return str.chars();
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: chars, expected exactly one argument, but got " + nGivenArgs;
        }
    }

    private static class Concatenation extends TFunction<TString, TString>
    {
        private Concatenation()
        {
            super("concat", "concat");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount > 0;
        }

        @Override
        @NotNull TString call(final @NotNull List<TString> args)
        {
            return args.stream().reduce(TString.EMPTY_STRING, TString::concat);
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: concatenation, expected at least one argument, zero given";
        }
    }
}