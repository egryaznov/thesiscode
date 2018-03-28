package foundation.lisp.exceptions;

public class InvalidTermException extends InterpreterException
{
    public InvalidTermException(final String message)
    {
        super(message);
    }
}
