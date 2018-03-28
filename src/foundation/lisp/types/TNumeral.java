package foundation.lisp.types;

import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

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



    public @NotNull TNumeral mul(final @NotNull TNumeral n)
    {
        // getValue() != null guaranteed
        assert getValue() != null : "Assert: TNumeral.mul, super.value is null!";
        assert n.getValue() != null : "Assert: TNumeral.mul, n.super.value is null!";
        return new TNumeral( getValue() * n.getValue() );
    }

   public @NotNull TNumeral mod(final @NotNull TNumeral modulo) throws InvalidTermException
   {
       assert getValue() != null : "Assert: mod, value is null!";
       assert modulo.getValue() != null : "Assert: mod, modulo.value is null!";
       final long mod = modulo.getValue();
       if (mod == 0)
       {
           throw new InvalidTermException("mod, second argument is zero!");
       }
       else
       {
           return new TNumeral(getValue() % mod);
       }
   }

    public @NotNull TNumeral add(final @NotNull TNumeral n)
    {
        // getValue() != null guaranteed
        assert getValue() != null : "Assert: TNumeral.mul, super.value is null!";
        assert n.getValue() != null : "Assert: TNumeral.mul, n.super.value is null!";
        return new TNumeral( getValue() + n.getValue() );
    }

    public static boolean isNumeral(final @NotNull String term)
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
        String mismatchMessage()
        {
            return "Arity mismatch: mod, expected exactly two arguments";
        }
    }


    private static class Addition extends TFunction<TNumeral, TNumeral>
    {
        private Addition()
        {
            super("+", "+");
        }

        @Override
        String mismatchMessage()
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

    private static class Multiplication extends TFunction<TNumeral, TNumeral>
    {
        private Multiplication()
        {
            super("*", "*");
        }

        @Override
        String mismatchMessage()
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
            assert argsArityMatch( args.size() ) : "Assert, TNumeral.call, arity mismatch: " + args.size();
            // Returns true iff a <= b
            final @NotNull TNumeral a = args.get(0);
            final @NotNull TNumeral b = args.get(1);
            assert a.getValue() != null : "Assert, TNumeral.call, a.getValue is null!";
            assert b.getValue() != null : "Assert, TNumeral.call, b.getValue is null!";
            return TBoolean.get( a.getValue() <= b.getValue() );
        }

        @Override
        String mismatchMessage()
        {
            return "Arity mismatch, LessOrEqual: expected 2 arguments";
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
        String mismatchMessage()
        {
            return "Arity mismatch, Less: expected 2 arguments";
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
        String mismatchMessage()
        {
            return "Arity mismatch, GreaterOrEqual: expected 2 arguments";
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
        String mismatchMessage()
        {
            return "Arity mismatch, Greater: expected 2 arguments";
        }
    }
}