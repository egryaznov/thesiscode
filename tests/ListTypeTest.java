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


class ListTypeTest
{
    private static final @NotNull Interpreter in   = new Interpreter("res/genealogies/my/genealogy.kindb");



    ListTypeTest()
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
    void headTest()
    {
        assertEquals("3", run("(head (list 3 2 1))"));
    }

    @Test
    void listLambdaTest()
    {
        assertEquals("2", run("((head (list (lambda () 2) (lambda () '2'))))"));
    }

    @Test
    void headTest1()
    {
        assertEquals("vacant", run("(head (list vacant (list 1)))"));
    }

    @Test
    void tailTest()
    {
        assertEquals("3", run("(head (tail (tail (list 1 2 3))))"));
    }

    @Test
    void listTest1()
    {
        assertEquals("0", run("(head (list 0 '1' 2 (date '1.1.1990') (lambda () 3) (list 4)))"));
    }

    @Test
    void atTest4()
    {
        assertEquals("void", run("(at (list 0 1 2 3 4 5) 7)"));
    }

    @Test
    void atTest3()
    {
        assertEquals("void", run("(at (list 0 1 2 3 4 5) -7)"));
    }

    @Test
    void atTest2()
    {
        assertEquals("2", run("(at (list 0 1 2 3 4 5) -4)"));
    }

    @Test
    void atTest1()
    {
        assertEquals("5", run("(at (list 0 1 2 3 4 5) -1)"));
    }

    @Test
    void atTest()
    {
        assertEquals("4", run("(at (list 0 1 2 3 4) 4)"));
    }

    @Test
    void appendTest0()
    {
        assertEquals("5", run("(head (append vacant 5))"));
    }

    @Test
    void appendTest2()
    {
        assertEquals("10", run("(head (append vacant 10 '9' ego ))"));
    }

    @Test
    void appendTest1()
    {
        assertEquals("1", run("(head (head (append vacant (list 1))))"));
    }

    @Test
    void countTest()
    {
        assertEquals("3", run("(count (append vacant 1 1 1))"));
    }

    @Test
    void countTest0()
    {
        assertEquals("0", run("(count vacant)"));
    }

    @Test
    void countTest2()
    {
        assertEquals("10", run("(count (join (list 1 2 3 4 5) (list 6 7 8 9 10)))"));
    }

    @Test
    void countTest1()
    {
        assertEquals("1", run("(count (append vacant vacant))"));
    }

    @Test
    void joinTest1()
    {
        assertEquals("(list 1 2 3 4 5)", run("(join (list 1 2 3) (list 4 5))"));
    }

    @Test
    void joinTest5()
    {
        assertEquals("(list 1 1 1)", run("(join (list 1) (list 1 1) )"));
    }
    @Test
    void joinTest4()
    {
        assertThrows(InterpreterException.class, new RunUnsafe("(join 1 2 3)"));
    }

    @Test
    void joinTest3()
    {
        assertEquals("(list 1 2 3 4 5)", run("(join (list 1) (list 2) (list 3) (list 4) (list 5))"));
    }

    @Test
    void joinTest2()
    {
        assertEquals("vacant", run("(join vacant vacant vacant vacant)"));
    }

    @Test
    void joinTest6()
    {
        assertEquals("(list 1)", run("(join (list 1))"));
    }

    @Test
    void joinTest()
    {
        assertEquals("(list 1 2 3)", run("(join vacant (list 1 2 3))"));
    }

    @Test
    void filterTest0()
    {
        assertEquals("(list 1 3 5 7 9)", run("(filter odd? (first-n 10))"));
    }

    @Test
    void filterTest1()
    {
        assertEquals("(list 0 1 2 3 4 5)", run("(filter (lambda (n) true) (first-n 5))"));
    }

    @Test
    void filterTest2()
    {
        assertEquals("vacant", run("(filter (lambda (n) false) (first-n 100))"));
    }

    @Test
    void mapTest0()
    {
        assertEquals("(list 1 2 3 4 5)", run("(map inc (list 0 1 2 3 4))"));
    }

    @Test
    void mapTest1()
    {
        assertEquals("(list 'a' 'ba' 'ca')", run("(map (lambda (s) (concat s 'a')) (list '' 'b' 'c'))"));
    }

    @Test
    void mapTest2()
    {
        assertEquals("(list (list vacant) (list 1) (list '2'))", run("(map (lambda (x) (list x)) (list vacant 1 '2'))"));
    }

    @Test
    void mapTest3()
    {
        assertEquals("(list 2 0)",
                run("(map apply (map (lambda (f) (twice f)) (list inc dec)) (list 0 2))"));
    }

    @Test
    void mapTest4()
    {
        assertEquals("vacant", run("(map id vacant vacant)"));
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