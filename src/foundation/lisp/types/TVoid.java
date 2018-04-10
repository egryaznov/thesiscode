package foundation.lisp.types;

import org.jetbrains.annotations.NotNull;

public class TVoid extends TObject<Object>
{
    public static final TVoid instance = new TVoid();

    private TVoid()
    {
        super(Type.VOID);
    }

    @Override
    public @NotNull String termToString()
    {
        return valueToString();
    }

    static boolean isVoid(final String literal)
    {
        return literal.equals( Type.VOID.getName() );
    }

}
