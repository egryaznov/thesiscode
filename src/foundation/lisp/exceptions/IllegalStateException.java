package foundation.lisp.exceptions;

public class IllegalStateException extends InterpreterException
{
    IllegalStateException(final String msg)
    {
        super(msg);
    }
}
