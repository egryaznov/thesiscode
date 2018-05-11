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

class StringTypeTest
{
    private static final @NotNull Interpreter in   = new Interpreter("res/genealogies/my/genealogy.kindb");



    StringTypeTest()
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
    void concatTest0()
    {
        assertEquals("'Hello, World!'", run("(concat 'Hello, ' 'World!')"));
    }

    @Test
    void concatTest1()
    {
        assertEquals("'one two three'", run("(concat 'one ' 'two ' 'three')"));
    }

    @Test
    void concatTest2()
    {
        assertEquals("'test'", run("(concat 'test')"));
    }

    @Test
    void charsTest0()
    {
        assertEquals("(list 'a' 'b' 'c' 'd')", run("(chars 'abcd')"));
    }

    @Test
    void charsTest1()
    {
        assertEquals("vacant", run("(chars '')"));
    }

    @Test
    void charsTest2()
    {
        assertEquals("(list 'v')", run("(chars 'v')"));
    }

    @Test
    void substrTest0()
    {
        assertEquals("'0123'", run("(substr '01234' 0 4)"));
    }

    @Test
    void substrTest1()
    {
        assertEquals("''", run("(substr '01234' 1 1)"));
    }

    @Test
    void substrTest2()
    {
        assertEquals("'12'", run("(substr '01234' 1 3)"));
    }

    @Test
    void substrTest3()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(substr 'test' -1 3)"));
    }

    @Test
    void substrTest4()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(substr 'test' 1 -3)"));
    }

    @Test
    void substrTest5()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(substr '0123' 10 100)"));
    }

    @Test
    void substrTest6()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(substr '0234' -10 -5)"));
    }

    @Test
    void stringTest0()
    {
        assertEquals("'-123'", run("(string -0123)"));
    }

    @Test
    void stringTest1()
    {
        assertEquals("'1234567890'", run("(string 1234567890)"));
    }

    @Test
    void stringTest2()
    {
        assertEquals("'1234'", run("(string 01234)"));
    }

    @Test
    void stringTest3()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(string ego)"));
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
