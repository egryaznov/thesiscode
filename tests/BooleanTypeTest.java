import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.types.TObject;
import foundation.lisp.types.TVoid;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class BooleanTypeTest
{
    private static final @NotNull Interpreter in   = new Interpreter("res/genealogies/my/genealogy.kindb");
    private              @NotNull String      term = "";



    BooleanTypeTest()
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

    private boolean runBool(final @NotNull String term)
    {
        return Boolean.valueOf(run(term));
    }



    @Test
    void boolAnd0()
    {
        assertTrue(runBool("(and true true)"));
    }

    @Test
    void boolAnd1()
    {
        assertFalse(runBool("(and false true)"));
    }

    @Test
    void boolAnd2()
    {
        assertFalse(runBool("(and (and true true) (and false false))"));
    }

    @Test
    void boolImplies0()
    {
        assertFalse(runBool("(implies true false)"));
    }

    @Test
    void boolImplies2()
    {
        assertTrue(runBool("(implies false true)"));
    }

    @Test
    void boolImplies1()
    {
        assertTrue(runBool("(implies (> 10 5) (> 10 4))"));
    }

    @Test
    void boolNot()
    {
        assertFalse(runBool("(not (not false))"));
    }

    @Test
    void boolNot1()
    {
        assertTrue(runBool("(not (not true))"));
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
