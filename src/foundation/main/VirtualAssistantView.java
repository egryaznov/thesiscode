package foundation.main;

import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.IllegalStateException;
import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class VirtualAssistantView extends JFrame
{
    private static final String VA_NAME = "Ami";
    private JTextArea outputArea;
    private JPanel mainPanel;
    private JTextField inputField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private final @NotNull GenealogyView genealogyView;
    private final @NotNull Interpreter in;

    VirtualAssistantView(final @NotNull GenealogyView genealogyView)
    {
        this.in = new Interpreter( genealogyView.getModel() );
        try
        {
            in.exec(new File("res/axioms.lisp"));
        } catch (InterpreterException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        this.genealogyView = genealogyView;
        setContentPane(mainPanel);
        setSize(600, 600);
        setResizable(false);
        setTitle("Ami: Virtual Assistant");
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(219, 87));
        echo("Hello, I'm Ami, your virtual assistant.", true);
        echo("I'm here to answer your queries.", true);
        echo("I understand both English and domain-specific lisp, my native language. ", true);
        echo(" However, when talking to me in English, please remember that I'm " +
                "not a real human, so I can fathom only certain question, but I will try my best anyway ;)", true);
        echo(" Type 'help' to see what I'm capable of.", true);
        sendButton.addActionListener(new SendAction());
        inputField.addKeyListener(new InputFieldKeyListener());
    }



    private void echo(final String text, final boolean fromAmi)
    {
        final String msg = String.format("   [%s]: %s", (fromAmi)? VA_NAME : "You", text);
        if ( !fromAmi )
        {
            outputArea.append("\n");
        }
        outputArea.append(msg);
        outputArea.append("\n");
    }

    private void respond(final String query)
    {
        echo(query, false);
        try
        {
            final String out;
            if ( in.isPrimitive(query) )
            {
                echo("You typed a lisp query. Here is the result of its evaluation:", true);
                out = in.exec( query ).toString();
            }
            else
            {
                final String clipped = in.clip(query);
                if (clipped.charAt(0) == '(')
                {
                    echo("You typed a lisp query. Here is the result of its evaluation:", true);
                    out = in.exec( clipped, true, true ).toString();
                }
                else
                {
                    out = "Sorry, I cannot understand your question." +
                            " Please, ascertain about what you can ask me in English by typing 'help' and retry.";
                }
            }
            echo(out, true);
        }
        catch (final InvalidTermException e1)
        {
            echo(e1.getMessage(), true);
        }
        catch (final IllegalStateException e1)
        {
            echo(e1.getMessage(), true);
            e1.printStackTrace();
        }
        catch (final InterpreterException e1)
        {
            echo("Global: " + e1.getMessage(), true);
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
            if (e.isControlDown() && (e.getKeyChar() == '\r'))
            {
                respond(inputField.getText());
                inputField.setText("");
            }
        }
    }
}
