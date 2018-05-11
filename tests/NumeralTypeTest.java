import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.types.TObject;
import foundation.lisp.types.TVoid;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumeralTypeTest
{
    private static final @NotNull Interpreter in   = new Interpreter("res/genealogies/my/genealogy.kindb");



    NumeralTypeTest()
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
        @NotNull TObject<?> result;
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
    void addTest0()
    {
        assertEquals("5", run("(+ 5)"));
    }

    @Test
    void addTest1()
    {
        assertEquals("10", run("(+ 1 9)"));
    }

    @Test
    void addTest2()
    {
        assertEquals("6", run("(+ 1 2 3)"));
    }

    @Test
    void addTest3()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(+ )"));
    }

    @Test
    void subTest0()
    {
        assertEquals("3", run("(- 3 0)"));
    }

    @Test
    void subTest1()
    {
        assertEquals("10",  run("(- 13 3)"));
    }

    @Test
    void subTest2()
    {
        assertEquals("-5", run("(- 0 5)"));
    }

    @Test
    void subTest3()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(- )"));
    }

    @Test
    void mulTest0()
    {
        assertEquals("10", run("(* 10)"));
    }

    @Test
    void mulTest1()
    {
        assertEquals("10", run("(* 2 5)"));
    }

    @Test
    void mulTest2()
    {
        assertEquals("30", run("(* 2 3 5)"));
    }

    @Test
    void mulTest3()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(* )"));
    }

    @Test
    void divTest0()
    {
        assertEquals("5", run("(div 10 2)"));
    }

    @Test
    void divTest1()
    {
        assertEquals("-1", run("(div 10 -10)"));
    }

    @Test
    void divTest2()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(div 19 0)"));
    }

    @Test
    void divTest3()
    {
        assertEquals("1", run("(div 10 9)"));
    }

    @Test
    void divTest4()
    {
        assertEquals("0", run("(div 1 10)"));
    }

    @Test
    void modTest0()
    {
        assertEquals("1", run("(mod 10 9)"));
    }

    @Test
    void modTest1()
    {
        assertEquals("0", run("(mod 1000 100)"));
    }

    @Test
    void modTest2()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(mod 5 0)"));
    }

    private class RunUnsafe implements Executable
    {
        private       @NotNull String   term = "";

        RunUnsafe(final @NotNull String term)
        {
            this.term = term;
        }


        public void setTerm(final @NotNull String term)
        {
            this.term = term;
        }

        @Override
        public void execute() throws InterpreterException
        {
            runUnsafe(this.term);
        }
    }
}
