package foundation;

import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.IllegalStateException;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.exceptions.InvalidTermException;
import foundation.lisp.exceptions.NotEnoughKnowledge;
import foundation.main.Database;
import foundation.main.MainFrame;
import foundation.main.Ontology;
import org.jetbrains.annotations.NotNull;

import java.awt.EventQueue;
import java.io.File;

public class Main
{
    public static void main(String[] args)
    {
        EventQueue.invokeLater(MainFrame::new);
        // m();
    }


    private static void m()
    {
        Database.instance.establishConnection( "res/genealogies/my/genealogy.kindb" );
        final @NotNull Interpreter in = new Interpreter( new Ontology() );
        System.out.println("\n------");
        final String query = "(kinship (person 'Евгений' 'Грязнов') (person 'Олег' 'Федотов'))";
        try
        {
            // TODO -- 1. Добавление новых типов и функций. 2. Включение интерпретатора в основную программу.
            in.exec(new File("res/axioms.lisp"));
            System.out.println( in.exec(query, false, true) );
            // System.out.println( in.splitByTerms(in.rewrite(query), true, true) );
            in.printLastBenchmark();
            Database.instance.closeConnection();
        }
        catch (final InvalidTermException | NotEnoughKnowledge e)
        {
            System.out.println(e.getMessage());
        }
        catch (final IllegalStateException e)
        {
            System.out.println(e.getMessage() + "\n");
            e.printStackTrace();
        }
        catch (final InterpreterException e)
        {
            e.printStackTrace();
        }
    }
}