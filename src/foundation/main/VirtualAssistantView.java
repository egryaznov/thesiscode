package foundation.main;

import foundation.entity.FamilyTree;
import foundation.entity.KinshipDictionary;
import foundation.entity.MaritalBond;
import foundation.entity.Person;
import foundation.entity.Vertex;
import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.IllegalInterpreterStateException;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class VirtualAssistantView extends JFrame
{
    private static final String VA_NAME = "Ami";
    private JTextArea outputArea;
    private JPanel mainPanel;
    private JTextField inputField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JLabel youLabel;
    private final @NotNull GenealogyView genealogyView;
    private final @NotNull Interpreter in;
    private @Nullable Vertex ego = null;
    private String lastMessage = "";
    private final @NotNull JSONObject lispDocJSON;

    VirtualAssistantView(final @NotNull GenealogyView genealogyView)
    {
        final int DEFAULT_WIDTH  = 339;
        final int DEFAULT_HEIGHT = 317;
        // Initialize fields
        this.in = new Interpreter( genealogyView.getModel() );
        this.genealogyView = genealogyView;
        this.lispDocJSON = loadLispDocumentation();
        // Automatically scroll the dialog down when new message is appended
        ((DefaultCaret)outputArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        // Customize the frame
        setContentPane(mainPanel);
        setAutoRequestFocus(true);
        setFocusable(true);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setTitle("Ami: Virtual Assistant");
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLocation(genealogyView.getWidth() - 150 + 10, genealogyView.getHeight() - 200 + 10);
        setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        // Print start-up text
        echo("Hello, I'm Ami, your virtual assistant.", true);
        echo("I'm here to answer your queries.", true);
        echo("I understand both English and domain-specific lisp, my native language. ", true);
        echo(" Type 'help' to see what I'm capable of.", true);
        // Register listeners
        sendButton.addActionListener(new SendAction());
        inputField.addKeyListener(new InputFieldKeyListener());
        // Automatically set focus on the input field every time the window shows up
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {
                super.windowOpened(e);
                inputField.requestFocusInWindow();
            }
        });
    }



    private @NotNull JSONObject loadLispDocumentation()
    {
        final @NotNull String jsonFileName = "res/lisp-doc.json";
        //
        final @Nullable InputStream jarIn = getClass().getResourceAsStream("/" + jsonFileName);
        final @NotNull File jsonFile = new File(jsonFileName);
        @NotNull JSONObject result = new JSONObject();
        if ( jarIn == null )
        {
            System.out.println("Cannot find json documentation in jar, trying to find it in: " + jsonFileName);
            if ( !jsonFile.exists() )
            {
                System.out.println("There is no .json either! Documentation will be empty!");
            }
            else
            {
                System.out.println("Found .json file, proceeding to load");
                try (final @NotNull BufferedReader br = new BufferedReader(new FileReader(jsonFile)))
                {
                    final @NotNull JSONParser parser = new JSONParser();
                    result = (JSONObject)parser.parse(br);
                }
                catch (final IOException | ParseException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            try (final @NotNull BufferedReader br = new BufferedReader(new InputStreamReader(jarIn)))
            {
                final @NotNull JSONParser parser = new JSONParser();
                result = (JSONObject)parser.parse(br);
            }
            catch (final IOException | ParseException e)
            {
                e.printStackTrace();
            }
            System.out.println("Successfully loaded .json documentation from jar");
        }
        //
        return result;
    }

    private void echo(final String text)
    {
        echo(text, true);
    }

    private void echo(final @NotNull String text, final boolean fromAmi)
    {
        final String msg = String.format("   [%s]: %s", (fromAmi)? VA_NAME : "YOU", text);
        if ( !fromAmi )
        {
            outputArea.append("\n");
        }
        outputArea.append(msg);
        outputArea.append("\n");
        if (!fromAmi)
        {
            outputArea.append("\n");
        }
    }

    private void respond(final @NotNull String userMessage)
    {
        if (userMessage.isEmpty())
        {
            return;
        }
        lastMessage = userMessage;
        echo(userMessage, false);
        // Lowercase and remove punctuation, if any
        final @NotNull StringBuilder sb = new StringBuilder(userMessage.toLowerCase());
        final char lastChar = sb.charAt(sb.length() - 1);
        final boolean isQuestion = (lastChar == '?');
        if ( lastChar == '.' || isQuestion || lastChar == '!')
        {
            sb.deleteCharAt(sb.length() - 1);
        }
        final @NotNull String query = sb.toString();
        // Choose how to respond to a query
        if (isQuery(query))
        {
            processQuery(query);
        }
        else if (isQuestion)
        {
            processQuestion(query);
        }
        else
        {
            processCommand(query);
        }
    }



    private void processQuestion(final @NotNull String question)
    {
        // question is already lowercased and all punctuation is removed from it
        // After splitting, `words` is guaranteed to have at least one item
        final @NotNull String[] words = question.toLowerCase().split(" ");
        //
        switch (words[0])
        {
            case "how" :
                howQuestion(words);
                break;
            case "who" :
                whoQuestion(words);
                break;
            case "where" :
                whereQuestion(words);
                break;
            default:
                unknownMessage(true);
                break;
        }
    }

    private void whereQuestion(final @NotNull String[] words)
    {
        // where <reference> (was | were) born
        //   0       1..N    wasWereIndex
        if ( words.length < 4 )
        {
            unknownMessage(true);
            return;
        }
        // Find the end of <reference>
        final int wasIndex = Arrays.asList(words).indexOf("was");
        final int wereIndex = Arrays.asList(words).indexOf("were");
        final int wasWereIndex = ( wasIndex == -1 )? wereIndex : wasIndex;
        if ( wasWereIndex == -1 )
        {
            unknownMessage(true);
            return;
        }
        //
        final @Nullable List<Vertex> reference = parseReference(words, 1, wasWereIndex - 1);
        if ( reference == null )
        {
            egoUnknown();
            return;
        }
        if ( reference.isEmpty() )
        {
            emptyReference(words, 1, wasWereIndex);
            return;
        }
        //
        for ( final Vertex person : reference )
        {
            final @NotNull String birthplace = person.profile().getOccupation();
            if ( birthplace.isEmpty() )
            {
                continue;
            }
            final @NotNull String firstName = person.profile().getFirstName();
            final @NotNull String lastName  = person.profile().getLastName();
            final @NotNull String response  = String.format("%s %s was born in %s.", firstName, lastName, birthplace);
            echo(response);
        }
    }

    private void howQuestion(final @NotNull String[] words)
    {
        if (words.length < 7)
        {
            unknownMessage(true);
            return;
        }
        // words.length is at least 7
        // Example questions:
        // 1. How am I related to <reference>?
        // 2. How is <reference> related to <reference>?
        int relatedIndex = -1;
        for (int i = 2; i < words.length; i++)
        {
            if (words[i].equals("related"))
            {
                relatedIndex = i;
                break;
            }
        }
        //
        if (relatedIndex == -1
                || relatedIndex + 2 >= words.length
                || !words[relatedIndex + 1].equals("to")
                || relatedIndex == 2)
        {
            unknownMessage(true);
            return;
        }
        // At this point there is at least one word between "is" and "related"
        // And at least one word between "to" and "?"
        final @Nullable List<Vertex> firstRef = parseReference(words, 2, relatedIndex - 1);
        final @Nullable List<Vertex> lastRef  = parseReference(words, relatedIndex + 2, words.length - 1);
        // Null ref signifies that variable this.ego is null, so we will exit
        if ( firstRef == null || lastRef == null )
        {
            return;
        }
        if ( firstRef.isEmpty() )
        {
            emptyReference(words, 2, relatedIndex);
            return;
        }
        if ( lastRef.isEmpty() )
        {
            final @NotNull String ref = Arrays.asList(words).subList(relatedIndex + 1, words.length).stream()
                                        .collect(Collectors.joining(" "));
            echo("I found no one who would match: '" + ref + "'");
            return;
        }
        // Evaluate kinship relation between the first person from the list `firstRef` and all people in `lastRef`
        final @NotNull Vertex from = firstRef.get(0);
        for ( Vertex to : lastRef )
        {
            final @NotNull List<String> toKinFrom = KinshipDictionary.instance.shorten( in.ontology().tree().kinship(to, from) );
            final @NotNull String prefix = (from.equals(ego))? "You are " :  from.profile().getFirstName() + " " + from.profile().getLastName() + " is a ";
            final @NotNull String ending = (to.equals(ego))? " of you" : " of " + to.profile().getFirstName() + " " + to.profile().getLastName();
            if ( toKinFrom.isEmpty() )
            {
                echo(String.format("%s and %s are not related.", prefix, ending));
                continue;
            }
            final @NotNull String message = toKinFrom.stream().collect(Collectors.joining(" of a ", prefix, ending));
            echo(message);
        }
    }

    private void whoQuestion(final @NotNull String[] words)
    {
        // Who is <reference>?
        // Who are <reference>?
        // Who am I?
        if ( words.length < 3 )
        {
            unknownMessage(true);
            return;
        }
        // words has at least 3 elements
        final @NotNull String article = words[1];
        //
        if ( !article.equals("am") && !article.equals("are") && !article.equals("is") )
        {
            unknownMessage(true);
        }
        //
        final @Nullable List<Vertex> reference = parseReference(words, 2, words.length - 1);
        if ( reference == null )
        {
            return;
        }
        if ( reference.isEmpty() )
        {
            emptyReference(words, 2, words.length);
            return;
        }
        // Echo full names of all relatives found in `reference`
        reference.stream()
                .map(relative -> relative.profile().getFirstName() + " " + relative.profile().getLastName())
                .forEach(this::echo);
    }

    private @Nullable List<Vertex> parseReference(final @NotNull String[] refWords, final int startInc, final int endInc)
    {
        // "I", "my <relative>", "my <relative>s' <relative>", "<full name>", "me", "myself"
        // (a | an) <relative> of <reference>
        final @Nullable List<Vertex> result;
        final @NotNull FamilyTree tree = in.ontology().tree();
        switch( refWords[startInc] )
        {
            case "myself" :
            case "me" :
            case "i" :
            {
                if  ( ego != null )
                {
                    // I, me and myself
                    result = new LinkedList<>();
                    result.add(this.ego);
                }
                else
                {
                    result = null;
                    egoUnknown();
                }
                break;
            }
            case "my" :
            {
                if  ( ego == null )
                {
                    result = null;
                    egoUnknown();
                    break;
                }
                final int nRelative = endInc - startInc;
                if ( nRelative == 2 )
                {
                    // Extract the two kinship terms
                    //      first        result
                    // my <relative>s' <relative>
                    final @NotNull StringBuilder sb = new StringBuilder( refWords[startInc + 1] );
                    // Remove needless letters from the first term
                    // Remove single quote (')
                    int firstLen = sb.length();
                    if ( sb.charAt(firstLen - 1) == '\'' )
                    {
                        sb.deleteCharAt(firstLen - 1);
                        firstLen--;
                    }
                    // Remove last letter 's'
                    if ( sb.charAt(firstLen - 1) == 's' )
                    {
                        sb.deleteCharAt(firstLen - 1);
                    }
                    final @NotNull String firstTerm = sb.toString();
                    // Find all first relatives
                    final @NotNull List<Vertex> first = tree.findRelatives(ego, firstTerm);
                    result = new LinkedList<>();
                    for (Vertex v : first)
                    {
                        // refWords[endInc] == last kinship term, as in
                        //      first        last
                        // my <relative>s' <relative>
                        result.addAll( tree.findRelatives(v, refWords[endInc]) );
                    }
                }
                else if ( nRelative == 1 )
                {
                    // my <relative>
                    result = tree.findRelatives(ego, refWords[startInc + 1]);
                }
                else
                {
                    result = new LinkedList<>();
                    unknownMessage(true);
                }
                break;
            }
            case "an" :
            case "a" :
            {
                // startInc (startInc + 1) (startInc + 2) (startInc + 3)
                //     a       <relative>         of       <reference>
                if ( endInc - startInc < 3 || !refWords[startInc + 2].equals("of") )
                {
                    result = new LinkedList<>();
                    unknownMessage(true);
                    break;
                }
                final @NotNull String relativeWord = refWords[startInc + 1];
                final @Nullable List<Vertex> egos = parseReference(refWords, startInc + 3, endInc);
                // NOTE: О null указателе должен сообщать этот метод, а о пустом result -- тот метод, который нас вызывает.
                if ( egos == null || egos.isEmpty() )
                {
                    // NOTE: Т.о., нам ничего не надо echo'ить потому, что о null мы уже сообщили, а о пустом result
                    // NOTE: сообщит вызывающий нас внешний метод.
                    result = egos;
                    break;
                }
                //
                result = new LinkedList<>();
                for ( final Vertex ego : egos )
                {
                    result.addAll( tree.findRelatives(ego, relativeWord) );
                }
                break;
            }
            default:
            {
                // <full name>
                final @NotNull String firstName = refWords[startInc];
                // Construct long last name
                final @NotNull var longNameBuilder = new StringBuilder();
                for (int i = startInc + 1; i <= endInc; i++)
                {
                    longNameBuilder.append(refWords[i]);
                    longNameBuilder.append(" ");
                }
                final int  lastIndex = longNameBuilder.length() - 1;
                final char lastChar = longNameBuilder.charAt( lastIndex );
                if ( lastChar == ' ' )
                {
                    // Remove trailing space
                    longNameBuilder.deleteCharAt( lastIndex );
                }
                final @NotNull String lastName = longNameBuilder.toString(); // NOTE: Long names are now supported!
                // Search for a relative
                result = new LinkedList<>();
                final @Nullable Vertex foundRelative = tree.getVertex(firstName, lastName);
                if ( foundRelative != null )
                {
                    result.add( foundRelative );
                }
                //
                break;
            }
        }
        // return only distinct elements
        return (result == null)? null : result.stream().distinct().collect(Collectors.toList());
    }

    private void emptyReference(final @NotNull String[] refWords, final int startInclusive, final int endExclusive)
    {
        final @NotNull String ref = Arrays.asList(refWords).subList(startInclusive, endExclusive).stream()
                .collect(Collectors.joining(" "));
        echo("I found no one who would match: " + ref);
    }

    private void egoUnknown()
    {
        echo("I don't know who you are, please introduce yourself and resend your query.");
        echo("Simply type: 'I'm <full name>'.");
    }

    private void unknownMessage(final boolean isQuestion)
    {
        if ( isQuestion )
        {
            echo("I'm sorry, I cannot understand your question.");
            echo("Type `questions` to see what you can ask me about.");
        }
        else
        {
            echo("I'm sorry, I can't fathom your command.");
            echo("Type `commands` to see what kind of order I can perform.");
        }
    }



    private void processQuery(final String query)
    {
        echo("You typed a lisp query. Here is the result of its evaluation:");
        try
        {
            echo( in.exec( query, false, true ).toString() );
        }
        catch (final InvalidTermException e1)
        {
            echo(e1.getMessage(), true);
        }
        catch (final IllegalInterpreterStateException e1)
        {
            echo(e1.getMessage(), true);
            e1.printStackTrace();
        }
        catch (final InterpreterException e1)
        {
            echo("Global: " + e1.getMessage(), true);
        }
    }

    private boolean isQuery(final String query)
    {
        if (query.isEmpty())
        {
            return false;
        }
        //
        boolean isQuery = false;
        try
        {
            if ( in.isPrimitive(query) )
            {
                isQuery = true;
            }
            else
            {
                final String clipped = in.clip(query);
                isQuery = (clipped.charAt(0) == '(');
            }
        }
        catch (InvalidTermException e)
        {
            echo(e.getMessage());
        }
        //
        return isQuery;
    }



    private void processCommand(final @NotNull String command)
    {
        if (command.isEmpty())
        {
            return;
        }
        //
        final String[] words = command.split(" ");
        final String firstWord = words[0].toLowerCase();
        switch (firstWord)
        {
            case "i" :
            case "i'm" :
                final @NotNull String egoFirstName = words[1];
                final @NotNull String egoLastName = words[2];
                final @Nullable Vertex v = in.ontology().tree().getVertex(egoFirstName, egoLastName);
                if ( v == null )
                {
                    echo( String.format("I can't find %s %s in my knowledge base.", egoFirstName, egoLastName) );
                }
                else
                {
                    this.ego = v;
                    echo( String.format("Nice to meet you, %s %s.", egoFirstName, egoLastName) );
                }
                break;
            case "set" :
                setCommand(words);
                break;
            case "benchmark" :
                final long millis = in.lastBenchmark() / 1000000;
                echo(String.format("%d ms.", millis));
                break;
            case "flush" :
                in.expungeCache();
                echo("Table of cached evaluated terms has been cleaned.");
                break;
            case "load" :
                loadCommand(words);
                break;
            case "clear" :
                outputArea.setText("");
                break;
            case "hide" :
                genealogyView.requestFocus();
                setVisible(false);
                break;
            case "show" :
                showCommand(words);
                break;
            case "print" :
                printCommand(words);
                break;
            case "help" :
                helpCommand(words);
                break;
            case "commands" :
                echo("Here's the list of all available commands:");
                echo("Everything inside squared brackets '[]' is optional");
                echo("1. Show [me] <reference>.");
                echo("2. 'commands'. This command.");
                echo("3. 'help'. Get help.");
                echo("4. 'clear'. Expunges the history of this dialog.");
                echo("5. 'load <lisp-file-name>'. Read a lisp file from disk and evaluates its content.");
                echo("6. 'hide'. Conceals this window.");
                break;
            case "questions":
                echo("Here's the list of all questions that I can answer:");
                echo("The question mark '?' at the end is required.");
                echo("1. How is <reference> related to <reference>?");
                echo("2. How am I related to <reference>?");
                echo("4. Who is <reference>?");
                echo("5. Who is (a or an) <kinship-term> of <reference>?");
                echo("6. Who am I?");
                echo("7. Where <reference> (was or were) born?");
                echo("Where '<reference>' is one of the following: ");
                // "I", "my <relative>", "my <relative>s' <relative>", "<full name>", "me", "myself"
                echo("1. I");
                echo("2. my <kinship-term>");
                echo("3. my <kinship-term>s' <kinship-term>");
                echo("4. Full name of a person");
                echo("5. me");
                echo("6. myself");
                echo("And '<kinship-term>' is either a father, or a sister, or an uncle and so on...");
                break;
            default:
                echo("I'm sorry, I cannot understand your English command. Please reformulate and try again.");
                break;
        }
    }

    private void setCommand(final @NotNull String[] words)
    {
        // words is ensured to be non-empty by the caller method "processCommand"
        if ( words.length < 2 )
        {
            echo("'Set' command: no option was specified.");
            return;
        }
        final @NotNull String option = words[1];
        switch (option)
        {
            case "cache" :
            case "caching" :
                in.enableCaching();
                echo("Term caching enabled.");
                break;
            case "nocache" :
            case "nocaching" :
                in.disableCaching();
                echo("Term caching disabled.");
                break;
            default :
                echo(String.format("'Set' command: unknown option '%s'", option));
                break;
        }
    }

    private void loadCommand(final @NotNull String[] words)
    {
        if (words.length < 2)
        {
            echo("Please specify the name of a lisp file you're trying to load.");
        }
        else
        {
            final @NotNull File axioms = new File(words[1]);
            if (axioms.exists())
            {
                try
                {
                    in.exec(axioms);
                    echo("Successfully loaded.");
                }
                catch (final InterpreterException e)
                {
                    echo(e.getMessage());
                }
            }
            else
            {
                echo(String.format("File %s does not exist.", words[1]));
            }
        }
    }

    private void showCommand(final @NotNull String[] words)
    {
        // [me] is optional
        // 1. Show [me] <reference>
        // 2. Show [me] recent events
        final int wordCount = words.length;
        if (wordCount < 2)
        {
            unknownMessage(false);
            return;
        }
        // The index in `words` of the first words of a <reference> in this command:
        // 1. Show [me] <reference>.
        int refStartIndex = 1;
        if ( words[1].equals("me") )
        {
            refStartIndex++;
        }
        // Show [me] <reference>
        final @Nullable List<Vertex> relatives = parseReference(words, refStartIndex, wordCount - 1);
        //
        if ( relatives == null )
        {
            return;
        }
        if ( relatives.isEmpty() )
        {
            emptyReference(words, refStartIndex, wordCount);
            return;
        }
        //
        final @NotNull Vertex firstRelative = relatives.get(0);
        echo( String.format("Centering on %s %s",
                firstRelative.profile().getFirstName(),
                firstRelative.profile().getLastName()) );
        genealogyView.centerOnPerson( firstRelative.profile() );
    }

    private void printCommand(final @NotNull String[] words)
    {
        // Print [me] [n] near events
        int refStartIndex = 1;
        if ( words[1].equals("me") )
        {
            refStartIndex++;
        }
        // Check if there is a number after the word 'Show' or 'me'
        final @NotNull String ref = words[refStartIndex];
        // Extract a limit on the number of dates to be printed, if there is any
        final int DEFAULT_PRINT_LIMIT = 10;
        final int printLimit;
        final @NotNull String forthcoming;
        if ( ref.matches("\\d+") )
        {
            printLimit = Integer.parseInt(ref);
            forthcoming = words[refStartIndex + 1];
        }
        else
        {
            printLimit = DEFAULT_PRINT_LIMIT;
            forthcoming = ref;
        }
        //
        if ( !forthcoming.equals("forthcoming") && !forthcoming.equals("impending") && !forthcoming.equals("near") )
        {
            unknownMessage(false);
            return;
        }
        // Show [me] (forthcoming | impending | near) events
        // Group all distinct forthcoming events by their date
        final @NotNull List<Occasion> events = new LinkedList<>();
        final @NotNull Map<Integer, Boolean> spouseSeen = new HashMap<>();
        in.ontology().getPeople().forEach( person ->
                {
                    events.add( BirthdayOccasion.of(person) );
                    final @Nullable MaritalBond marriage = in.ontology().getMaritalBond(person);
                    if ( marriage != null )
                    {
                        final int id1 = marriage.getHead().getID();
                        final int id2 = marriage.getTail().getID();
                        if ( !spouseSeen.containsKey(id1) && !spouseSeen.containsKey(id2) )
                        {
                            events.add( AnniversaryOccasion.of(marriage) );
                            spouseSeen.put(id1, true);
                        }
                    }
                });
        events.stream()
                .filter(Occasion::isImpeding)
                .sorted()
                .distinct()
                .limit(printLimit)
                .forEach(event -> echo(event.toString()));
    }

    @SuppressWarnings("unchecked")
    private void helpCommand(final @NotNull String[] words)
    {
        if ( words.length == 2 )
        {
            //
            final @NotNull String funcName = words[1];
            final @Nullable JSONObject doc = (JSONObject)lispDocJSON.get(funcName);
            if ( doc == null )
            {
                echo("There is no such lisp function: " + funcName);
                return;
            }
            // name
            echo("Name: " + doc.get("Name"));
            // signature
            echo("Signature: " + doc.get("Signature"));
            // desc
            echo("Description: " + doc.get("Description"));
            // arguments
            echo("Arguments:");
            final @NotNull JSONObject args = (JSONObject) doc.get("Arguments");
            //noinspection unchecked
            args.forEach( (key, value) -> echo("   " + key + " : " + value) );
            // examples
            echo("Examples:");
            final @NotNull JSONArray examples = (JSONArray) doc.get("Examples");
            examples.forEach(str -> echo("   " + str.toString()));
            // type
            echo("Type: " + doc.get("Type"));
            // notes
            echo("Notes: " + doc.get("Notes"));
        }
        else
        {
            echo("There are three types of messages which I can understand:", true);
            echo("1. Commands in English. For instance, ");
            echo("To see a list of all acceptable commands, type 'commands'.");
            echo("2. Questions in English. You can ask me how your relatives, including you, are related to each other.");
            echo("To see a list of all acceptable questions, type 'questions'.");
            echo("3. Queries in Lisp. I'll parse and evaluate your lisp query.");
            echo("I accept all well-formed lisp queries.");
            echo("Type 'help lisp-func' to get the documentation of a particular lisp function 'lisp-func'");
        }
    }



    private static abstract class Occasion implements Comparable<Occasion>
    {
        final int day;
        final int month;
        final int id;

        Occasion(final int id, final int day, final int month)
        {
            this.id = id;
            this.day = day;
            this.month = month;
        }

        boolean isImpeding()
        {
            final int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            final int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            if ( this.month == currentMonth )
            {
                return this.day >= currentDay;
            }
            else
            {
                return this.month > currentMonth;
            }
        }

        @NotNull String monthToStr(final int month)
        {
            final @NotNull Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, month);
            return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
        }

        @Override
        public int compareTo(final @NotNull VirtualAssistantView.Occasion o)
        {
            if ( this.month == o.month )
            {
                return this.day - o.day;
            }
            else
            {
                return this.month - o.month;
            }
        }

        @Override
        public boolean equals(Object obj)
        {
            if ( !(obj instanceof Occasion) )
            {
                return false;
            }
            //
            return ((Occasion) obj).id == this.id;
        }

    }

    private static class BirthdayOccasion extends Occasion
    {
        final @NotNull String celebrantFullName;
        final int age;

        BirthdayOccasion(final int id, final int day, final int month, final @NotNull String name, final int age)
        {
            super(id, day, month);
            this.celebrantFullName = name;
            this.age = age;
        }

        @Override
        public String toString()
        {
            final int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            final int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            final @NotNull String dateStr;
            if ( this.day == currentDay && this.month == currentMonth )
            {
                dateStr = "TODAY";
            }
            else
            {
                dateStr = String.format("%d-th of %s", day, monthToStr(month));
            }
            return String.format("%s: %d-th Birthday of %s", dateStr, age, celebrantFullName);
        }

        @Override
        public boolean equals(Object obj)
        {
            if ( !(obj instanceof BirthdayOccasion) )
            {
                return false;
            }
            //
            return super.equals(obj);
        }

        static @NotNull BirthdayOccasion of(final @NotNull Person p)
        {
            final int id = p.getID();
            final @NotNull Calendar birthDate = Person.stringToCalendar(p.getDateOfBirth());
            final int day = birthDate.get(Calendar.DAY_OF_MONTH);
            final int month = birthDate.get(Calendar.MONTH);
            final int age = Calendar.getInstance().get(Calendar.YEAR)  - birthDate.get(Calendar.YEAR);
            final @NotNull String name = p.getFirstName() + " " + p.getLastName();
            //
            return new BirthdayOccasion(id, day, month, name, age);
        }

    }

    private static class AnniversaryOccasion extends Occasion
    {
        final int wifeId;
        final int anniversary;
        final @NotNull String firstSpouseFullName;
        final @NotNull String secondSpouseFullName;

        AnniversaryOccasion(final int husbandId, final int wifeId, final int day, final int month, final int anniversary,
                            final @NotNull String aName,
                            final @NotNull String bName)
        {
            super(husbandId, day, month);
            this.wifeId = wifeId;
            this.anniversary = anniversary;
            this.firstSpouseFullName = aName;
            this.secondSpouseFullName = bName;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if ( !(obj instanceof AnniversaryOccasion) )
            {
                return false;
            }
            //
            return super.equals(obj) && ((AnniversaryOccasion) obj).wifeId == wifeId;
        }

        @Override
        public String toString()
        {
            final int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            final int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            final @NotNull String dateStr;
            if ( this.day == currentDay && this.month == currentMonth )
            {
                dateStr = "TODAY";
            }
            else
            {
                dateStr = String.format("%d-th of %s", day, monthToStr(month));
            }
            return String.format("%s: %d-th Wedding Anniversary of %s and %s",
                    dateStr,
                    anniversary,
                    firstSpouseFullName,
                    secondSpouseFullName);
        }

        static @NotNull AnniversaryOccasion of(final @NotNull MaritalBond marriage)
        {
            final int husbandID = (marriage.getHead().isMale())? marriage.getHead().getID() : marriage.getTail().getID();
            final int wifeID    = (marriage.getHead().isMale())? marriage.getTail().getID() : marriage.getHead().getID();
            final @NotNull Calendar weddingDate = Person.stringToCalendar( (marriage).getDateOfWedding() );
            final int day = weddingDate.get(Calendar.DAY_OF_MONTH);
            final int month = weddingDate.get(Calendar.MONTH);
            final int anniversary = Calendar.getInstance().get(Calendar.YEAR) - weddingDate.get(Calendar.YEAR);
            final @NotNull String aName = marriage.getHead().getFirstName() + " " + marriage.getHead().getLastName();
            final @NotNull String bName = marriage.getTail().getFirstName() + " " + marriage.getTail().getLastName();
            //
            return new AnniversaryOccasion(husbandID, wifeID, day, month, anniversary, aName, bName);
        }
    }



    private class SendAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            respond(inputField.getText());
        }
    }



    private class InputFieldKeyListener implements KeyListener
    {
        @Override
        public void keyPressed(KeyEvent e)
        {
            // nop
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
            // nop
        }

        @Override
        public void keyTyped(KeyEvent e)
        {
            final char typedChar = e.getKeyChar();
            switch (typedChar)
            {
                case '\r' :
                    if ( e.isControlDown() )
                    {
                        respond(inputField.getText());
                        inputField.setText("");
                    }
                    break;
                case '\u000B' :
                    if ( e.isControlDown() )
                    {
                        inputField.setText(lastMessage);
                    }
                    break;
            }
        }
    }
}
