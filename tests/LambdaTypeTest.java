import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.types.TObject;
import foundation.lisp.types.TVoid;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class LambdaTypeTest
{
    private static final @NotNull Interpreter in   = new Interpreter("res/genealogies/my/genealogy.kindb");
    private              @NotNull String      term = "";


    LambdaTypeTest()
    {
        try
        {
            in.exec(new File("res/axioms.lisp"), null);
            run("(define implies (lambda (x y) (or (not x) y)))");
        }
        catch (InterpreterException e)
        {
            System.out.println(e.getMessage() + "\n");
        }
    }

    private @NotNull String runUnsafe(final @NotNull String term) throws InterpreterException
    {
        @NotNull TObject<?> result = TVoid.instance;
        result = in.exec(term, true, false);
        return result.termToString();
    }

    private @NotNull String run(final @NotNull String term)
    {
        @NotNull TObject<?> result = TVoid.instance;
        try
        {
            result = in.exec(term);
        }
        catch (InterpreterException e)
        {
            System.out.println(e.getMessage() + "\n");
        }
        //
        return result.termToString();
    }

    @Test
    void lambdaTest0()
    {
        assertEquals("4", run("((compose square double) 1)"));
    }

    @Test
    void lambdaTest1()
    {
        assertEquals("8", run("((compose (compose double double) double) 1)"));
    }

    @Test
    void lambdaTest2()
    {
        assertEquals("1024", run("((times-n double 10) 1)"));
    }


    private class RunUnsafe implements Executable
    {
        @Override
        public void execute() throws InterpreterException
        {
            runUnsafe(term);
        }
    }
}
