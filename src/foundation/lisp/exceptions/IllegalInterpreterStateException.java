package foundation.lisp.exceptions;

public class IllegalInterpreterStateException extends InterpreterException
{
    IllegalInterpreterStateException(final String msg)
    {
        super(msg);
    }
}
