package foundation.main;

import foundation.entity.Person;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame
{
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;
    /*
        Contains the ON/OFF boolean values of all the graphics options
     */
    public static final Map<String, Boolean> graphicsOptions = new HashMap<>();
    static final String TITLE = "Kindred: Genealogy Tree";
    static final String SHAPE_ANTIALIASING = "Shape Antialiasing";
    static final String TEXT_ANTIALIASING = "Text Antialiasing";
    public static final String FILLED_CIRCLES = "Filled Circles";
    static final String ANIMATION = "Animation";
    private @Nullable GenealogyView genealogyView;
    private @Nullable VirtualAssistantView vaView;

    public MainFrame()
    {
        graphicsOptions.put(SHAPE_ANTIALIASING, true);
        graphicsOptions.put(TEXT_ANTIALIASING, true);
        graphicsOptions.put(FILLED_CIRCLES, true);
        graphicsOptions.put(ANIMATION, true);
        initMainMenu();
        this.setTitle(TITLE);
        // NOTE: Resizable true?
        this.setResizable(true);
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setLocationRelativeTo(null);
        this.addWindowListener(new ClosingWindowListener());
        setVisible(true);
    }

    private void initMainMenu()
    {
        // Construct Options menu
        final @NotNull JMenu jmOptions = new JMenu("Options");
        final @NotNull JCheckBoxMenuItem jmiShapeAntialiasing = new JCheckBoxMenuItem(SHAPE_ANTIALIASING, true);
        final @NotNull JCheckBoxMenuItem jmiTextAntialiasing = new JCheckBoxMenuItem(TEXT_ANTIALIASING, true);
        final @NotNull JCheckBoxMenuItem jmiFilledCircles = new JCheckBoxMenuItem(FILLED_CIRCLES, true);
        final @NotNull JCheckBoxMenuItem jmiAnimation = new JCheckBoxMenuItem(ANIMATION, true);
        jmOptions.add(jmiAnimation);
        jmOptions.add(jmiFilledCircles);
        jmOptions.add(jmiTextAntialiasing);
        jmOptions.add(jmiShapeAntialiasing);
        // Register Options menu listeners
        final @NotNull OptionsMenuListener listener = new OptionsMenuListener();
        jmiShapeAntialiasing.addActionListener(listener);
        jmiTextAntialiasing.addActionListener(listener);
        jmiFilledCircles.addActionListener(listener);
        jmiAnimation.addActionListener(listener);
        // Construct File menu
        final @NotNull JMenu jmFile = new JMenu("File");
        final @NotNull JMenuItem jmiQuit = new JMenuItem("Quit");
        final @NotNull JMenuItem jmiNewGenealogy = new JMenuItem("New Genealogy");
        final @NotNull JMenuItem jmiOpenGenealogy = new JMenuItem("Open Genealogy");
        final @NotNull JMenuItem jmiSaveAll = new JMenuItem("Save All");
        jmFile.add(jmiNewGenealogy);
        jmFile.add(new JSeparator());
        jmFile.add(jmiOpenGenealogy);
        jmFile.add(jmiSaveAll);
        jmFile.add(new JSeparator());
        jmFile.add(jmiQuit);
        // Set CMD-Key shortcuts to items in 'File' menu
        jmiNewGenealogy.setAccelerator( KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK) );
        jmiSaveAll.setAccelerator( KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK) );
        jmiOpenGenealogy.setAccelerator( KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK) );
        // Register File menu listeners
        jmiNewGenealogy.addActionListener(new NewGenealogyMenuListener());
        jmiOpenGenealogy.addActionListener(new OpenGenealogyMenuListener());
        jmiSaveAll.addActionListener(new SaveAllMenuListener());
        jmiQuit.addActionListener(new QuitMenuListener());
        // Construct Virtual Assistant Menu
        final @NotNull JMenu jmAmi = new JMenu("Virtual Assistant");
        final @NotNull JMenuItem jmiAssistant = new JMenuItem("Open");
        // Set CMD-A shortcut on the "Open VA" item
        jmiAssistant.setAccelerator( KeyStroke.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK) );
        jmAmi.add(jmiAssistant);
        // Register Assistant menu listener
        jmiAssistant.addActionListener(new OpenVirtualAssistant());
        // Construct a main menu bar
        final @NotNull JMenuBar jmb = new JMenuBar();
        // Add Options and File menu to the main menu bar
        jmb.add(jmFile);
        jmb.add(jmOptions);
        jmb.add(jmAmi);
        // Add the menu bar itself to the main frame
        setJMenuBar(jmb);
    }

    /*
        Initialize the genealogy genealogyView in which we will draw the family tree.
     */
    private void initView()
    {
        if (genealogyView != null)
        {
            genealogyView.stopAnimation();
            MainFrame.this.remove(genealogyView);
        }
        genealogyView = new GenealogyView();
        add(genealogyView);
        // MainFrame.this.pack();
        this.setLocationRelativeTo(null);
    }

    /*
        Updates all the rows in tables 'person' and 'photo' with information stored in each node.
     */
    private void commitNodesToDatabase()
    {
        if (genealogyView != null)
        {
            final @NotNull Ontology model = genealogyView.getModel();
            for (final Person person : model.getPeople())
            {
                model.commitToDatabase(person);
            }
        }
    }



    private class OpenVirtualAssistant implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if ( vaView == null )
            {
                if (genealogyView == null)
                {
                    final String msg = "Open or create a genealogy first";
                    JOptionPane.showMessageDialog(MainFrame.this, msg, "Notice", JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    vaView = new VirtualAssistantView(genealogyView);
                    vaView.setVisible(true);
                }
            }
            else
            {
                vaView.setVisible(true);
            }
        }
    }

    private class OptionsMenuListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
            graphicsOptions.put(item.getText(), item.getState());
        }
    }

    private class QuitMenuListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            MainFrame.this.dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING));
        }
    }

    private class ClosingWindowListener extends WindowAdapter
    {
        @Override
        public void windowClosing(WindowEvent e)
        {
            super.windowClosing(e);
            //
            commitNodesToDatabase();
            System.exit(0);
        }
    }

    private class SaveAllMenuListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            commitNodesToDatabase();
        }
    }

    private class NewGenealogyMenuListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            final @NotNull var DEFAULT_GEN_DIR = "/res/genealogies/";
            final @NotNull String currentDirName = System.getProperty("user.dir");
            System.out.println(currentDirName);
            @Nullable String genealogyTitle = "";
            boolean dirSuccessfullyCreated = false;
            @NotNull var msg = "Enter the title of your new genealogy:";
            while (!dirSuccessfullyCreated)
            {
                genealogyTitle = JOptionPane.showInputDialog(
                        MainFrame.this,
                        msg,
                        "Title Input",
                        JOptionPane.INFORMATION_MESSAGE
                );
                if (genealogyTitle == null)
                {
                    break;
                }
                final @NotNull var newSubDir = new File(currentDirName + DEFAULT_GEN_DIR + genealogyTitle);
                dirSuccessfullyCreated = !genealogyTitle.isEmpty() && newSubDir.mkdirs();
                msg = "Cannot create a genealogy with such title, please enter another:";
            }
            if ( genealogyTitle != null )
            {
                Database.instance.createNewDatabase(currentDirName + DEFAULT_GEN_DIR + genealogyTitle);
                initView();
            }
        }
    }

    private class OpenGenealogyMenuListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            final @NotNull JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose the Genealogy File");
            chooser.setCurrentDirectory(new File("res/genealogies"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("Kindred Genealogy Files", "kindb"));
            chooser.setControlButtonsAreShown(false);
            chooser.setDragEnabled(true);
            final int status = chooser.showOpenDialog(MainFrame.this);
            if ( status == JFileChooser.APPROVE_OPTION )
            {
                Database.instance.establishConnection( chooser.getSelectedFile().getAbsolutePath() );
                initView();
            }
        }
    }
}