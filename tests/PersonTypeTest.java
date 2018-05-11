import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.types.TObject;
import foundation.lisp.types.TVoid;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonTypeTest
{
    private static final @NotNull Interpreter in   = new Interpreter("res/genealogies/my/genealogy.kindb");



    PersonTypeTest()
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
    void childrenTest0()
    {
        assertEquals("(list (person 'Евгений' 'Грязнов'))", run("(children (mother ego))"));
    }

    @Test
    void childrenTest1()
    {
        assertEquals("(list (person 'Иван' 'Грязнов') (person 'Екатерина' 'Грязнова'))",
                run("(children (person 'Сергей Грязнов'))"));
    }

    @Test
    void childrenTest2()
    {
        assertEquals("vacant", run("(children ego)"));
    }

    @Test
    void fatherTest0()
    {
        assertEquals("vacant", run("(father ego)"));
    }

    @Test
    void fatherTest1()
    {
        assertEquals("(list (person 'Сергей' 'Грязнов'))", run("(father (person 'Иван Грязнов'))"));
    }

    @Test
    void fatherTest2()
    {
        assertEquals("(list (person 'Валентин' 'Грязнов'))", run("(father (mother ego))"));
    }

    @Test
    void motherTest0()
    {
        assertEquals("(list (person 'Светлана' 'Грязнова'))", run("(mother ego)"));
    }

    @Test
    void genDistTest0()
    {
        assertEquals("0", run("(gen-dist ego ego)"));
    }

    @Test
    void genDistTest1()
    {
        assertEquals("1", run("(gen-dist ego (head (mother ego)))"));
    }

    @Test
    void genDistTest2()
    {
        assertEquals("2", run("(gen-dist ego (head (father (mother ego))))"));
    }

    @Test
    void genDistTest3()
    {
        assertEquals("3", run("(gen-dist ego (head (father (father (mother ego)))))"));
    }

    @Test
    void genDistTest4()
    {
        assertEquals("-1", run("(gen-dist (head (mother ego)) ego)"));
    }

    @Test
    void attrTest0()
    {
        assertEquals("'Евгений Грязнов'", run("(attr ego 'full name')"));
    }
    
    @Test
    void attrTest1()
    {
        assertEquals("void", run("(attr ego 'date of wedding')"));
    }

    @Test
    void attrTest2()
    {
        assertEquals("'male'", run("(attr ego 'sex')"));
    }

    @Test
    void attrTest3()
    {
        assertEquals("'female'", run("(attr (head (mother ego)) 'sex')"));
    }

    @Test
    void attrTest4()
    {
        assertEquals("(date '30.11.1995')", run("(attr ego 'birth')"));
    }

    @Test
    void kinshipTest0()
    {
        assertEquals("vacant", run("(kinship (person 'ольга васильевна балалаева') ego)"));
    }

    @Test
    void kinshipTest1()
    {
        assertEquals("vacant", run("(kinship ego ego)"));
    }

}
