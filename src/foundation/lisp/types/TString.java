package foundation.lisp.types;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

// NOTE: Value этого класса хранится как строка без одинарных кавычек,
public class TString extends TObject<String>
{
    public static final TString EMPTY_STRING = new TString("", false);

    // NOTE: Pass only quoted strings here!!!
    private TString(final String value)
    {
        this(value, true);
    }

    TString(final String value, final boolean quoted)
    {
        super(Type.STRING);
        setValue( quoted? removeQuotes(value) : value );
    }



    /*
        Removes single quotes from the string `s`.
        For example, removeQuotes("'hello'") = "hello"
     */
    static String removeQuotes(final String s)
    {
        final int len = s.length();
        return s.substring(1, len - 1);
    }

    private String quote(final String s)
    {
        return "'" + s + "'";
    }

    public @NotNull TString concat(final @NotNull TString s)
    {
        // getValue() != null guaranteed
        assert ( getValue() != null ) : "Assert: TString.concat, getValue() is NULL";
        assert ( s.getValue() != null ) : "Assert: TString.concat, s.getValue is NULL";
        final String s1 = getValue();
        final String s2 = s.getValue();
        return new TString( s1.concat(s2), false );
    }

    public static boolean isString(final String term)
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
        String mismatchMessage()
        {
            return "Arity Mismatch: concatenation, expected at least one argument, zero given";
        }
    }
}