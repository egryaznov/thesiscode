import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.types.TBoolean;
import foundation.lisp.types.TObject;
import foundation.lisp.types.TVoid;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTypeTest
{
    private static final @NotNull Interpreter in   = new Interpreter("res/genealogies/my/genealogy.kindb");
    private              @NotNull String      term = "";



    DateTypeTest()
    {
        try
        {
            in.exec(new File("res/axioms.lisp"), null);
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
    void dateTest1()
    {
        this.term = "(date '1.1.199')";
        assertThrows(InterpreterException.class, new RunUnsafe());
    }

    @Test
    void before2()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(before (date '02.02.1990') (date '02.03.1990'))"));
    }

    @Test
    void before1()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(before (date '02.01.1989') (date '02.01.1990'))"));
    }

    @Test
    void before()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(before (date '01.01.1990') (date '02.01.1990'))"));
    }

    @Test
    void afterTest2()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(after (date '01.02.1990') (date '30.01.1990'))"));
    }

    @Test
    void afterTest()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(after (date '02.01.1990') (date '01.01.1990'))"));
    }

    @Test
    void afterTest1()
    {
        assertEquals(TBoolean.FALSE_KEYWORD, run("(after (date '01.01.1990') (date '01.01.1990'))"));
    }

    @Test
    void duringTest2()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(during (date '01.01.1900') (date '01.01.1899') (date '01.01.1901'))"));
    }

    @Test
    void duringTest0()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(during (date '01.02.1899') (date '01.01.1899') (date '01.03.1899'))"));
    }

    @Test
    void duringTest4()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(during (date '01.03.1899') (date '01.01.1899') (date '01.03.1899'))"));
    }

    @Test
    void duringTest3()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(during (date '01.01.1899') (date '01.01.1899') (date '01.03.1899'))"));
    }

    @Test
    void duringTest1()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(during (date '02.01.1899') (date '01.01.1899') (date '03.01.1899'))"));
    }

    @Test
    void dateLambda()
    {
        run("(define pobeda (date '09.05.1945'))");
        //
        assertEquals(TBoolean.TRUE_KEYWORD, run("(before pobeda now)"));
    }

    @Test
    void dateMonth()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(= 2 (month (date '01.02.1990')))"));
    }

    @Test
    void dateYear()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(= 1990 (year (date '01.02.1990')))"));
    }

    @Test
    void dateDay()
    {
        assertEquals(TBoolean.TRUE_KEYWORD, run("(= 31 (day (date '31.01.1990')))"));
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