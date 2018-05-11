package foundation;

import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.IllegalInterpreterStateException;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.exceptions.InvalidTermException;
import foundation.lisp.exceptions.NotEnoughKnowledge;
import foundation.lisp.types.TObject;
import foundation.main.Database;
import foundation.main.MainFrame;
import foundation.main.Ontology;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.EventQueue;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Main
{
    private static final @NotNull String QUIT_KEYWORD           = "quit";
    private static final @NotNull String TIME_KEYWORD           = "time";
    private static final @NotNull String OPTION_KEY_GEN_FILE    = "g";
    private static final @NotNull String OPTION_KEY_AXIOMS_FILE = "a";



    public static void main(final @NotNull String[] args)
    {
        if ( args.length > 0 )
        {
            final @NotNull Map<String, String> argMap = parseCmdArguments(args);
            if ( !argMap.containsKey(OPTION_KEY_GEN_FILE) )
            {
                System.out.println("Please provide a genealogy file for the KISP interpreter with '-g' option to enter REPL mode");
                System.out.println("REPL mode won't work without being provided with a genealogy file first");
                System.exit(1);
            }
            // The title for the genealogy that will be used by KISP
            final @NotNull String genealogyTitle = argMap.get(OPTION_KEY_GEN_FILE);
            // A .lisp file with KISP terms that will be loaded before launching REPL mode
            // Useful when we need to quickly introduce new definitions
            final @Nullable File definitionsToPreload;
            if ( argMap.containsKey(OPTION_KEY_AXIOMS_FILE) )
            {
                System.out.println(String.format("Entering REPL mode with the genealogy %s and the axioms file %s",
                                                    genealogyTitle,
                                                    argMap.get(OPTION_KEY_AXIOMS_FILE))
                );
                definitionsToPreload = new File( argMap.get(OPTION_KEY_AXIOMS_FILE) );
            }
            else
            {
                System.out.println(String.format("Entering REPL mode with the genealogy %s", genealogyTitle));
                definitionsToPreload = null;
            }
            // We've gathered all options, now let's run REPL
            System.out.println(String.format("Type '%s' to show the evaluation time of the last term.", TIME_KEYWORD));
            System.out.println(String.format("Type '%s' to exit.", QUIT_KEYWORD));
            launchREPL(genealogyTitle, definitionsToPreload);
        }
        else
        {
            EventQueue.invokeLater(MainFrame::new);
        }
    }

    private static void launchREPL(final @NotNull String genealogyTitle, final @Nullable File axiomsFile)
    {
        final @NotNull var in = new Interpreter(genealogyTitle);
        if ( axiomsFile != null )
        {
            try
            {
                in.exec(axiomsFile, null);
            }
            catch (InterpreterException e)
            {
                System.out.println(e.getMessage() + "\n");
            }
        }
        //
        final @NotNull Scanner inputScanner = new Scanner(System.in);
        boolean isQuitNotTyped = true;
        while ( isQuitNotTyped )
        {
            System.out.print("|-  ");
            final @NotNull String curLine = inputScanner.nextLine();
            if ( curLine.equals(QUIT_KEYWORD) )
            {
                isQuitNotTyped = false;
                continue;
            }
            if ( curLine.equals(TIME_KEYWORD) )
            {
                final double MILLION = 1000000L; // Six zeroes
                final double millis = in.lastBenchmark() / MILLION;
                System.out.println(String.format("Eval Time: %.4f s", millis));
                continue;
            }
            //
            try
            {
                final @NotNull TObject<?> evalResult = in.exec(curLine, true, true);
                System.out.println(evalResult.valueToString());
            }
            catch (final InterpreterException e)
            {
                System.out.println(e.getMessage() + "\n");
            }
        }
    }

    /**
     * Transforms an array of program arguments into a \(map : String \to String\).
     * Its keys are option names prefixed by dashes: "i" in "-i" or "a" in "-a", and whose values are values of these options.
     * So, for example, an array {@code ["-h", "-i", "title", "-a", "filename"]} will be transformed into a
     * {@code map = { "h" : "", "i" : "title", "a" : "filename", "none" : ""}}. As you can see, there is a special key
     * named "none", which point to a value that was passed without any option keys. This method allows only a SINGLE
     * value per option key.
     *
     * @param args a string array of program arguments that need to be parsed.
     * @return A map that maps option keys ("-i", "-a", etc.) to their corresponding values.
     */
    private static @NotNull Map<String, String> parseCmdArguments(final @NotNull String[] args)
    {
        final @NotNull var result = new HashMap<String, String>();
        @NotNull String underKey = "none";
        for (final String arg : args)
        {
            // Checks whether we are at an option key
            if (arg.charAt(0) == '-')
            {
                if ( !result.containsKey(underKey) )
                {
                    // There were no values for previous option key, so place an empty string to indicate that this key exists
                    result.put(underKey, "");
                }
                underKey = arg.substring(1);
            }
            else
            {
                // NOTE: We allow only one value per option key
                result.putIfAbsent(underKey, arg);
            }
        }
        //
        return result;
    }

    @SuppressWarnings("unused")
    private static void m()
    {
        Database.instance.establishConnection( "res/genealogies/my/genealogy.kindb" );
        final @NotNull Interpreter in = new Interpreter( new Ontology() );
        System.out.println("\n------");
        final String query = "(kinship (person 'Евгений' 'Грязнов') (person 'Олег' 'Федотов'))";
        try
        {
            in.exec(new File("res/axioms.lisp"), null);
            System.out.println( in.exec(query, false, true) );
            // System.out.println( in.splitByTerms(in.rewrite(query), true, true) );
            in.printLastBenchmark();
            Database.instance.closeConnection();
        }
        catch (final InvalidTermException | NotEnoughKnowledge e)
        {
            System.out.println(e.getMessage());
        }
        catch (final IllegalInterpreterStateException e)
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