package foundation.lisp.types;

import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class TFunction<T, R> extends TObject<String>
{
    private final String name;

    TFunction(final String name, final String term)
    {
        super(Type.FUNCTION, term);
        this.name = name;
    }



    abstract boolean argsArityMatch(final int argsCount);

    abstract @NotNull R call(final @NotNull List<T> args) throws InterpreterException;

    abstract String mismatchMessage();

    public @NotNull R apply(final @NotNull List<T> args) throws InterpreterException
    {
        final @NotNull R result;
        if ( argsArityMatch(args.size()) )
        {
            try
            {
                result = call(args);
            }
            catch (final ClassCastException e)
            {
                throw new InvalidTermException( parseCastErrorMessage(e.getMessage()) + " in argument list: " + args);
            }
        }
        else
        {
            throw new InvalidTermException(mismatchMessage());
        }
        //
        return result;
    }

    private String parseCastErrorMessage(final String msg)
    {
        final String[] words = msg.split(" ");
        final String gotType = words[0];
        final int gotLastDotIndex = gotType.lastIndexOf('.');
        final int nWords = words.length;
        final String expectedType = words[nWords - 1];
        final int expectedLastDotIndex = expectedType.lastIndexOf('.');
        return String.format("Type Mismatch: expected a %s, got a %s",
                expectedType.substring(expectedLastDotIndex + 1, expectedType.length()),
                gotType.substring(gotLastDotIndex + 1, gotType.length())
        );
    }

    @Override
    public String valueToString()
    {
        return getName();
    }

    public String getName()
    {
        return name;
    }
}