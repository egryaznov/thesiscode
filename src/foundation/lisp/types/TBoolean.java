package foundation.lisp.types;

import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

// XXX: Conditional AND/OR or logical?
// XXX: For now it's *conditional*
public class TBoolean extends TObject<Boolean>
{
    public static final String TRUE_KEYWORD = "true";
    public static final String FALSE_KEYWORD = "false";
    private static final TBoolean TRUE = new TBoolean(true);
    static final TBoolean FALSE = new TBoolean(false);

    private TBoolean(final boolean value)
    {
        super(Type.BOOLEAN, value);
    }



    private @NotNull TBoolean not()
    {
        // this.geValue() is ensured to be not null
        assert ( this.getValue() != null ) : "Assert: TBoolean.not, the `super.value` is NULL";
        return new TBoolean( !this.getValue() );
    }

    public @NotNull TBoolean or(final @NotNull TBoolean bool)
    {
        // this.geValue() is ensured to be not null
        assert ( this.getValue() != null ) : "Assert: TBoolean.or, the `super.value` is NULL";
        assert ( bool.getValue() != null ) : "Assert: TBoolean.or, the `bool.value` is NULL";
        return new TBoolean(this.getValue() || bool.getValue());
    }

    public @NotNull TBoolean and(final @NotNull TBoolean bool)
    {
        // this.geValue() is ensured to be not null
        assert ( this.getValue() != null ) : "Assert: TBoolean.and, the `super.value` is NULL";
        assert ( bool.getValue() != null ) : "Assert: TBoolean.and, the `bool.value` is NULL";
        return new TBoolean(this.getValue() && bool.getValue());
    }

    static boolean isBoolean(final String literal)
    {
        return literal.equals(TRUE_KEYWORD) || literal.equals(FALSE_KEYWORD);
    }

    public boolean passes()
    {
        assert ( this.getValue() != null ) : "Assert: TBoolean.passes, the `super.value` is NULL";
        return this.getValue();
    }

    @SuppressWarnings("unused")
    public boolean fails()
    {
        assert ( this.getValue() != null ) : "Assert: TBoolean.fails, the `super.value` is NULL";
        return !this.getValue();
    }

    @SuppressWarnings("unused")
    static @NotNull TBoolean parseBoolean(final String primitive) throws InvalidTermException
    {
        // NOTE: Why not just use a constructor instead of this method?
        // NOTE: Because we don't want to allow creating instances of this class,
        // NOTE: but only returning already instantiated TRUE or FALSE.
        switch (primitive)
        {
            case TRUE_KEYWORD:
                return TRUE;
            case FALSE_KEYWORD:
                return FALSE;
            default:
                // NOTE: Everything other from 'true' or 'false' is considered a lisp-level error.
                throw new InvalidTermException("Error: '" + primitive + "' is not a valid boolean primitive," + "should be 'true' or 'false'");
        }
    }

    @SuppressWarnings("unused")
    public static @NotNull TBoolean get(final boolean primitive)
    {
        return (primitive)? TRUE : FALSE;
    }

    @Override
    public @NotNull String termToString()
    {
        return valueToString();
    }

    public static void registerAtomicFunctions(final @NotNull Map<String, TFunction> dict)
    {
        final @NotNull TFunction and = new LogicalAND();
        dict.put( and.getName(), and );
        final @NotNull TFunction or = new LogicalOR();
        dict.put( or.getName(), or );
        final @NotNull TFunction not = new LogicalNOT();
        dict.put( not.getName(), not );
    }



    private static class LogicalAND extends TFunction<TBoolean, TBoolean>
    {
        private LogicalAND()
        {
            super("and", "and");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount > 0;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TBoolean> args)
        {
            // args.size > 0 at this point
            return args.stream().reduce(TBoolean.TRUE, TBoolean::and);
        }

        @Override
        String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch: and, expected at least 1 argument, zero given";
        }
    }

    private static class LogicalNOT extends TFunction<TBoolean, TBoolean>
    {
        private LogicalNOT()
        {
            super("not", "not");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TBoolean> args)
        {
            assert args.size() == 1 : "Assert: LogicalNOT.call, the number of passed arguments is " + args.size() + ", but should be 1";
            return args.get(0).not();
        }

        @Override
        String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch: not, expected exactly one argument, but got " + nGivenArgs;
        }
    }

    private static class LogicalOR extends TFunction<TBoolean, TBoolean>
    {
        private LogicalOR()
        {
            super("or", "or");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount > 0;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TBoolean> args)
        {
            // args.size > 0 at this point
            return args.stream().reduce(TBoolean.FALSE, TBoolean::or);
        }

        @Override
        String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch: or, expected at least one argument, zero given";
        }
    }
}
