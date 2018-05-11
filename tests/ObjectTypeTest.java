import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.types.TObject;
import foundation.lisp.types.TVoid;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;

import static foundation.lisp.types.TBoolean.FALSE_KEYWORD;
import static foundation.lisp.types.TBoolean.TRUE_KEYWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ObjectTypeTest
{
    private static final @NotNull Interpreter in   = new Interpreter("res/genealogies/my/genealogy.kindb");



    ObjectTypeTest()
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
    void equalsTest0()
    {
        assertEquals(TRUE_KEYWORD, run("(= 5 5)"));
        assertEquals(TRUE_KEYWORD, run("(= true true)"));
        assertEquals(FALSE_KEYWORD, run("(= false true)"));
    }

    @Test
    void equalsTest1()
    {
        assertEquals(TRUE_KEYWORD, run("(= (date '1.1.1990') (date '01.01.1990'))"));
    }

    @Test
    void equalsTest2()
    {
        assertEquals(TRUE_KEYWORD, run("(= 'test' 'test')"));
    }

    @Test
    void equalsTest3()
    {
        assertEquals(FALSE_KEYWORD, run("(= 'case' 'CaSe')"));
    }

    @Test
    void equalsTest4()
    {
        assertEquals(TRUE_KEYWORD, run("(= void void)"));
    }

    @Test
    void equalsTest5()
    {
        assertEquals(TRUE_KEYWORD, run("(= vacant vacant)"));
    }

    @Test
    void equalsTest6()
    {
        assertEquals(TRUE_KEYWORD, run("(= (list 1 2 3) (list 1 2 3))"));
    }

    @Test
    void equalsTest7()
    {
        assertEquals(FALSE_KEYWORD, run("(= (list 3 2 1) (list 1 2 3))"));
    }

    @Test
    void equalsTest8()
    {
        assertEquals(TRUE_KEYWORD, run("(= ego (person 'Евгений Грязнов'))"));
    }

    @Test
    void equalsTest9()
    {
        assertEquals(FALSE_KEYWORD, run("(= ego (person 'Иван Грязнов'))"));
    }

    @Test
    void ofTypeTest0()
    {
        assertEquals(TRUE_KEYWORD, run("(of-type? void 'void')"));
    }

    @Test
    void ofTypeTest1()
    {
        assertEquals(TRUE_KEYWORD, run("(of-type? 5 'numerAl')"));
    }

    @Test
    void ofTypeTest2()
    {
        assertEquals(TRUE_KEYWORD, run("(of-type? 5 'numerAl')"));
    }

    @Test
    void ofTypeTest3()
    {
        assertEquals(TRUE_KEYWORD, run("(of-type? true 'boolean')"));
        assertEquals(TRUE_KEYWORD, run("(of-type? false 'boolean')"));
    }

    @Test
    void ofTypeTest4()
    {
        assertEquals(TRUE_KEYWORD, run("(of-type? vacant 'list')"));
    }

    @Test
    void ofTypeTest5()
    {
        assertEquals(TRUE_KEYWORD, run("(of-type? 'test' 'string')"));
    }

    @Test
    void ofTypeTest6()
    {
        assertEquals("true", run("(of-type? ego 'person')"));
    }

    @Test
    void ofTypeTest7()
    {
        assertEquals("true", run("(of-type? of-type? 'function')"));
    }

    @Test
    void ofTypeTest8()
    {
        assertEquals("true", run("(of-type? (lambda () 2) 'function')"));
    }

    @Test
    void ofTypeTest9()
    {
        assertEquals("false", run("(of-type? vacant 'test')"));
    }

    @Test
    void ofTypeTest10()
    {
        assertEquals("true", run("(of-type? (list 3) 'list')"));
    }

}
