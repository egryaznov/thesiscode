package foundation.lisp.types;

public class TVoid extends TObject<Object>
{
    public static final TVoid instance = new TVoid();

    private TVoid()
    {
        super(Type.VOID);
    }

    public static boolean isVoid(final String literal)
    {
        return literal.equals( Type.VOID.getName() );
    }

}
