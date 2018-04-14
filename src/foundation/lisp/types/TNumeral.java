package foundation.lisp.types;

import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TNumeral extends TObject<Long>
{
    TNumeral(final String numeral)
    {
        super(Type.NUMERAL, Long.parseLong(numeral));
    }

    TNumeral(final long n)
    {
        super(Type.NUMERAL, n);
    }


    /**
     * Calculates the difference between this number (minuend) and {@code n} (subtrahend).
     * @param n  subtrahend.
     * @return Difference.
     */
    private @NotNull TNumeral sub(final @NotNull TNumeral n)
    {
        assert this.getValue() != null : "Assert: TNumeral.sub, this.value is null!";
        assert n.getValue() != null : "Assert: TNumeral.sub, n.value is null!";
        return new TNumeral( getValue() - n.getValue() );
    }

    private @NotNull TNumeral mul(final @NotNull TNumeral n)
    {
        // getValue() != null guaranteed
        assert getValue() != null : "Assert: TNumeral.mul, super.value is null!";
        assert n.getValue() != null : "Assert: TNumeral.mul, n.super.value is null!";
        return new TNumeral( getValue() * n.getValue() );
    }

    /**
     * Performs an integer division: floor(a / b), where numerator (a) is this class, and denominator, (b) is n.
     * @param n divisor
     * @return The quotient of the integer division floor(this / n)
     */
    private @NotNull TNumeral div(final @NotNull TNumeral n) throws InvalidTermException
    {
        assert this.getValue() != null : "Assert: TNumeral.sub, this.value is null!";
        assert n.getValue() != null : "Assert: TNumeral.sub, n.value is null!";
        if ( n.getValue() == 0 )
        {
            throw new InvalidTermException("div, divisor is zero!");
        }
        return new TNumeral( getValue() / n.getValue() );
    }

    private @NotNull TNumeral mod(final @NotNull TNumeral modulo) throws InvalidTermException
   {
       assert getValue() != null : "Assert: mod, value is null!";
       assert modulo.getValue() != null : "Assert: mod, modulo.value is null!";
       final long mod = modulo.getValue();
       if (mod == 0)
       {
           throw new InvalidTermException("mod, second argument is zero!");
       }
       return new TNumeral(getValue() % mod);
   }

    private @NotNull TNumeral add(final @NotNull TNumeral n)
    {
        // getValue() != null guaranteed
        assert getValue() != null : "Assert: TNumeral.mul, super.value is null!";
        assert n.getValue() != null : "Assert: TNumeral.mul, n.super.value is null!";
        return new TNumeral( getValue() + n.getValue() );
    }

    static boolean isNumeral(final @NotNull String term)
    {
        final char firstChar = term.charAt(0);
        if ( (firstChar != '-') && (isNotDigit(firstChar)) )
        {
            return false;
        }
        if ( (firstChar == '-') && (term.length() == 1) )
        {
            return false;
        }
        //
        boolean result = true;
        for (int i = 1; i < term.length(); i++)
        {
            if (isNotDigit(term.charAt(i)))
            {
                result = false;
                break;
            }
        }
        //
        return result;
    }

    private static boolean isNotDigit(final char chr)
    {
        return (chr < '0') || (chr > '9');
    }

    @Override
    public @NotNull String termToString()
    {
        return valueToString();
    }

    public static void registerAtomicFunctions(final @NotNull Map<String, TFunction> dict)
    {
        // Addition
        final @NotNull TFunction addition = new Addition();
        dict.put( addition.getName(), addition );
        // Multiplication
        final @NotNull TFunction multiplication = new Multiplication();
        dict.put( multiplication.getName(), multiplication );
        // LessOrEqual
        final @NotNull TFunction lessOrEqual = new LessOrEqual();
        dict.put( lessOrEqual.getName(), lessOrEqual );
        // Less
        final @NotNull TFunction less = new Less();
        dict.put( less.getName(), less );
        // GreaterOrEqual
        final @NotNull TFunction greaterOrEqual = new GreaterOrEqual();
        dict.put( greaterOrEqual.getName(), greaterOrEqual );
        // Greater
        final @NotNull TFunction greater = new Greater();
        dict.put( greater.getName(), greater );
        // Modulo
        final @NotNull TFunction mod = new Modulo();
        dict.put( mod.getName(), mod );
        // Subtraction
        final @NotNull TFunction sub = new Subtraction();
        dict.put( sub.getName(), sub );
        // Division
        final @NotNull TFunction div = new Division();
        dict.put(div.getName(), div);
    }



    private static class Modulo extends TFunction<TNumeral, TNumeral>
    {
        Modulo()
        {
            super("mod", "mod");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TNumeral call(final @NotNull List<TNumeral> args) throws InvalidTermException
        {
            // args.size() == 2
            final @NotNull TNumeral n = args.get(0);
            final @NotNull TNumeral modulo = args.get(1);
            //
            return n.mod(modulo);
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch: mod, expected exactly two arguments, but got " + nGivenArgs;
        }
    }

    private static class Addition extends TFunction<TNumeral, TNumeral>
    {
        private Addition()
        {
            super("+", "+");
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: addition, expected at least one argument, zero given";
        }

        @Override
        public @NotNull TNumeral call(final @NotNull List<TNumeral> args)
        {
            return args.stream().reduce(new TNumeral(0), TNumeral::add);
        }

        @Override
        public boolean argsArityMatch(final int argsCount)
        {
            return argsCount > 0;
        }
    }

    private static class Subtraction extends TFunction<TNumeral, TNumeral>
    {
        Subtraction()
        {
            super("-", "-");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TNumeral call(final @NotNull List<TNumeral> args)
        {
            // args.size == 2
            final @NotNull TNumeral minuend = args.get(0);
            final @NotNull TNumeral subtrahend = args.get(1);
            return minuend.sub( subtrahend );
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: sub, expected exactly 2 arguments, but got " + nGivenArgs;
        }
    }

    private static class Multiplication extends TFunction<TNumeral, TNumeral>
    {
        private Multiplication()
        {
            super("*", "*");
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: multiplication, expected at least one argument, zero given";
        }

        @Override
        public @NotNull TNumeral call(final @NotNull List<TNumeral> args)
        {
            return args.stream().reduce(new TNumeral(1), TNumeral::mul);
        }

        @Override
        public boolean argsArityMatch(final int argsCount)
        {
            return argsCount > 0;
        }
    }

    private static class Division extends TFunction<TNumeral, TNumeral>
    {
        Division()
        {
            super("div", "div");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TNumeral call(final @NotNull List<TNumeral> args) throws InvalidTermException
        {
            // args.size == 2
            final @NotNull TNumeral dividend = args.get(0);
            final @NotNull TNumeral divisor = args.get(1);
            return dividend.div( divisor );
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: div, expected exactly 2 arguments, but got " + nGivenArgs;
        }
    }

    private static class LessOrEqual extends TFunction<TNumeral, TBoolean>
    {
        LessOrEqual()
        {
            super("<=", "<=");
        }


        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TNumeral> args)
        {
            // args.size == 2
            // Returns true iff a <= b
            final @NotNull TNumeral a = args.get(0);
            final @NotNull TNumeral b = args.get(1);
            Objects.requireNonNull(a.getValue());
            Objects.requireNonNull(b.getValue());
            return TBoolean.get( a.getValue() <= b.getValue() );
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch, LessOrEqual: expected 2 arguments, but got " + nGivenArgs;
        }
    }

    private static class Less extends TFunction<TNumeral, TBoolean>
    {
        Less()
        {
            super("<", "<");
        }


        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TNumeral> args)
        {
            assert argsArityMatch( args.size() ) : "Assert, TNumeral.call, arity mismatch: " + args.size();
            // Returns true iff a < b
            final @NotNull TNumeral a = args.get(0);
            final @NotNull TNumeral b = args.get(1);
            assert a.getValue() != null : "Assert, TNumeral.call, a.getValue is null!";
            assert b.getValue() != null : "Assert, TNumeral.call, b.getValue is null!";
            return TBoolean.get( a.getValue() < b.getValue() );
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch, Less: expected 2 arguments, but got " + nGivenArgs;
        }
    }

    private static class GreaterOrEqual extends TFunction<TNumeral, TBoolean>
    {
        GreaterOrEqual()
        {
            super(">=", ">=");
        }


        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TNumeral> args)
        {
            assert argsArityMatch( args.size() ) : "Assert, TNumeral.call, arity mismatch: " + args.size();
            // Returns true iff a >= b
            final @NotNull TNumeral a = args.get(0);
            final @NotNull TNumeral b = args.get(1);
            assert a.getValue() != null : "Assert, TNumeral.call, a.getValue is null!";
            assert b.getValue() != null : "Assert, TNumeral.call, b.getValue is null!";
            return TBoolean.get( a.getValue() >= b.getValue() );
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch, GreaterOrEqual: expected 2 arguments, but got " + nGivenArgs;
        }
    }

    private static class Greater extends TFunction<TNumeral, TBoolean>
    {
        Greater()
        {
            super(">", ">");
        }


        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TNumeral> args)
        {
            assert argsArityMatch( args.size() ) : "Assert, TNumeral.call, arity mismatch: " + args.size();
            // Returns true iff a > b
            final @NotNull TNumeral a = args.get(0);
            final @NotNull TNumeral b = args.get(1);
            assert a.getValue() != null : "Assert, TNumeral.call, a.getValue is null!";
            assert b.getValue() != null : "Assert, TNumeral.call, b.getValue is null!";
            return TBoolean.get( a.getValue() > b.getValue() );
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch, Greater: expected 2 arguments, but got " + nGivenArgs;
        }
    }
}