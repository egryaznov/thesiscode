package foundation.lisp.exceptions;

public class NotEnoughKnowledge extends InterpreterException
{
    NotEnoughKnowledge(final String msg)
    {
        super(msg);
    }
}
