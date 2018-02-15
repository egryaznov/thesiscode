package foundation;

import org.jetbrains.annotations.NotNull;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.EventQueue;

public class Main
{

    public static void main(String[] args)
    {
        EventQueue.invokeLater(() ->
        {
            final @NotNull JFrame genealogyFrame = new MainFrame();
            genealogyFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            genealogyFrame.setVisible(true);
        });
    }

}
