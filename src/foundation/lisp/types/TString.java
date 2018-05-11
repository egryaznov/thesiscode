package foundation.lisp.types;

import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.exceptions.InvalidTermException;
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
            try
            {
                // NOTE: substring may throw an exception
                // NOTE: but here it will never happen, since [i, i+1) is always a correct interval
                chars.add(this.substring(i, i + 1));
            }
            catch (InvalidTermException e)
            {
                e.printStackTrace();
            }
        }
        //
        return new TList(chars);
    }

    private @NotNull TString substring(final int beginIndexInclusive, final int endIndexExclusive) throws InvalidTermException
    {
        assert getValue() != null : "Assert TString.substring, value is null!";
        if ( beginIndexInclusive < 0
                || endIndexExclusive < 0
                || (beginIndexInclusive > endIndexExclusive)
                || (beginIndexInclusive >= getValue().length())
                || (endIndexExclusive > getValue().length()))
        {
            throw new InvalidTermException(String.format("Substring function, invalid arguments, begin: %d, end: %d",
                    beginIndexInclusive,
                    endIndexExclusive));
        }
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
        //
        final @NotNull var substring = new Substring();
        dict.put( substring.getName(), substring );
        //
        final @NotNull var toString = new ToString();
        dict.put( toString.getName(), toString );
    }

    @Override
    public @NotNull String termToString()
    {
        return "'" + valueToString() + "'";
    }



    private static class ToString extends TFunction<TNumeral, TString>
    {

        ToString()
        {
            super("string", "string");
        }

        @Override
        boolean argsArityMatch(final int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TString call(final @NotNull List<TNumeral> args)
        {
            final @NotNull var numeral = (TNumeral)args.get(0);
            return new TString( numeral.termToString(), false );
        }

        @Override
        String mismatchMessage(int nGivenArgs)
        {
            return "Arity mismatch: string, expected exactly one argument, but got " + nGivenArgs;
        }
    }

    private static class Substring extends TFunction<TObject<?>, TString>
    {
        Substring()
        {
            super("substr", "substr");
        }

        @Override
        boolean argsArityMatch(final int argsCount)
        {
            return (argsCount == 2) || (argsCount == 3);
        }

        @NotNull
        @Override
        TString call(final @NotNull List<TObject<?>> args) throws InterpreterException
        {
            final @NotNull TString  result;
            final @NotNull TString  source              = (TString) args.get(0);
            final @NotNull TNumeral beginIndexInclusive = (TNumeral)args.get(1);
            //
            assert beginIndexInclusive.getValue() != null : "Assert: substr, beginIndex.value is null!";
            // args.size is either 2 or 3
            if ( args.size() == 3 )
            {
                final @NotNull TNumeral endIndexExclusive = (TNumeral)args.get(2);
                assert endIndexExclusive.getValue() != null : "Assert: substr, endIndex.value is null!";
                //
                result = source.substring(
                        beginIndexInclusive.getValue().intValue(),
                        endIndexExclusive.getValue().intValue()
                );
            }
            else
            {
                assert source.getValue() != null : "Assert: substr, source.value is null!";
                //
                final int sourceLength = source.getValue().length();
                result = source.substring(
                        beginIndexInclusive.getValue().intValue(),
                        sourceLength
                );
            }
            //
            return result;
        }

        @Override
        String mismatchMessage(final int nGivenArgs)
        {
            return "Args mismatch: substr, expected either 2 or three arguments, but got " + nGivenArgs;
        }
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