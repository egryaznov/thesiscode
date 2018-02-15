package foundation;

import org.jetbrains.annotations.NotNull;

import javax.swing.JFrame;

class MainFrame extends JFrame
{
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;

    MainFrame()
    {
        this.initFrame();
    }

    /*
        Initialize the main window in which we will draw the family tree
     */
    private void initFrame()
    {
        final @NotNull GenealogyView view = new GenealogyView();
        this.add( view );
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setTitle("Kindred: Genealogy Tree");
        this.setLocationRelativeTo(null);
        //
        new GenealogyPresenter(view);
    }
}