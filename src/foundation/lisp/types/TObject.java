package foundation.lisp.types;

import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class TObject<T>
{
    private final @NotNull Type type;
    private @Nullable T value;

    public TObject(final @NotNull Type type, final @Nullable T value)
    {
        this.type = type;
        this.value = value;
    }

    public TObject(final @NotNull Type type)
    {
        this(type, null);
    }




    protected  @Nullable T getValue()
    {
        return value;
    }

    void setValue(final @Nullable T value)
    {
        this.value = value;
    }

    public @NotNull Type getType()
    {
        return type;
    }

    public String valueToString()
    {
        return ( value == null )? Type.VOID.getName() : value.toString();
    }

    public boolean instanceOf(final Type type)
    {
        return this.type.equals(type);
    }

    @Override
    public String toString()
    {
        return String.format("%s : %s", type.getName(), valueToString());
    }

    @Override
    public boolean equals(final Object obj)
    {
        final boolean equals;
        if ((obj != null) && (obj instanceof TObject<?>))
        {
            final @NotNull TObject<?> tObj = (TObject<?>)obj;
            final boolean valuesEqual;
            if (this.value == null)
            {
                valuesEqual = tObj.value == null;
            }
            else
            {
                valuesEqual = this.value.equals( tObj.value );
            }
            equals = valuesEqual && this.type.equals(tObj.type);
        }
        else
        {
            equals = false;
        }
        //
        return equals;
    }

    public boolean isVoid()
    {
        return instanceOf(Type.VOID);
    }

    public static @NotNull TObject<?> parsePrimitive(final String literal) throws InvalidTermException
    {
        @Nullable TObject<?> result = null;
        for (Type type : Type.values())
        {
            if ( type.canParse(literal) )
            {
                result = type.parse(literal);
                break;
            }
        }
        //
        if ( result == null )
        {
            throw new InvalidTermException("Cannot parse the following literal: " + literal);
        }
        //
        return result;
    }

    public static void registerAtomicFunctions(final @NotNull Map<String, TFunction> dict)
    {
        final @NotNull TFunction equals = new Equals();
        dict.put( equals.getName(), equals );
        //
        final @NotNull TFunction ofType = new OfType();
        dict.put( ofType.getName(), ofType );
    }



    // NOTE: First argument is a TObject, second is TString
    private static class OfType extends TFunction<TObject<?>, TBoolean>
    {
        OfType()
        {
            super("of-type?", "of-type?");
        }


        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TObject<?>> args)
        {
            // NOTE: args.size() == 2
            final @NotNull TObject<?> patient = args.get(0);
            final @NotNull TString rawType = (TString)args.get(1);
            assert patient.getValue() != null : "Assert: OfType.call, patient.value is null";
            assert rawType.getValue()    != null : "Assert: OfType.call, type.value is null";
            @Nullable Type type = null;
            for (Type t : Type.values())
            {
                if (t.getName().equals(rawType.getValue()))
                {
                    type = t;
                }
            }
            if (type == null)
            {
                return TBoolean.FALSE;
            }
            else
            {
                return TBoolean.get( patient.instanceOf(type) );
            }
        }

        @Override
        String mismatchMessage()
        {
            return null;
        }
    }

    private static class Equals extends TFunction<TObject<?>, TBoolean>
    {
        private Equals()
        {
            super("=", "=");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TObject<?>> args)
        {
            assert argsArityMatch(args.size()) : "Assert: Equals.call, args mismatch";
            return TBoolean.get( args.get(0).equals(args.get(1)) );
        }

        @Override
        String mismatchMessage()
        {
            return "Arity mismatch: =, expected exactly 2 arguments";
        }
    }

}
