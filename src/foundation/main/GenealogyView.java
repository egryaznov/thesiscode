package foundation.main;

import foundation.entity.Bond;
import foundation.entity.Person;
import foundation.lisp.types.TDate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Timer;
import java.util.TimerTask;

public class GenealogyView extends JPanel
{
    static final int DEFAULT_WIDTH = 1200;
    static final int DEFAULT_HEIGHT = 800;
    public static final int DEFAULT_CIRCLE_DIAMETER = 50;
    public static final int DEFAULT_CIRCLE_RADIUS = DEFAULT_CIRCLE_DIAMETER / 2;
    private static final @NotNull Color BACKGROUND_COLOR = new Color(153, 217, 234);
    private final @NotNull IncrementTask increment = new IncrementTask();
    private final @NotNull Timer animationTimer = new Timer(true);
    private final @NotNull Ontology model = new Ontology();
    private final @NotNull ProfileFrame profileFrame = new ProfileFrame(this);
    private final @NotNull Camera camera;

    GenealogyView()
    {
        this.camera = new Camera(this);
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setBackground(BACKGROUND_COLOR);
        // Register listeners
        final @NotNull CanvasMouseListener mouseListener = new CanvasMouseListener();
        addMouseListener( mouseListener );
        addMouseMotionListener( mouseListener );
        addMouseWheelListener( new CanvasMouseWheelListener() );
        // Schedule animation timer
        animationTimer.schedule(increment, 0, 50);
    }


    /*
        Constructs and shows at (x, y) the "Add Person"/"Remove Person" popup menu.
     */
    private void showNodePopupMenu(final int x, final int y)
    {
        final String label;
        final @NotNull ActionListener action;
        final @Nullable Person personUnderCursor = detectNode(x, y);
        if ( personUnderCursor != null )
        {
            label = "Remove Person";
            action = new RemovePersonPopupListener(personUnderCursor);
        }
        else
        {
            label = "Add Person";
            action = new AddPersonPopupListener(x, y);
        }
        // Create menu item and add action listener to it
        final @NotNull JMenuItem jmiAction = new JMenuItem(label);
        jmiAction.addActionListener(action);
        // Create and show the popup menu
        final @NotNull JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(jmiAction);
        popupMenu.show(this, x, y);
    }

    /*
        Cancels animation timer.
     */
    void stopAnimation()
    {
        animationTimer.cancel();
    }

    /*
        Constructs and shows at (x, y) the bond popup menu between `head` and `tail`.
     */
    private void showBondPopupMenu(final @NotNull Person head, final @NotNull Person tail, final int x, final int y)
    {
        final @NotNull JPopupMenu popupMenu = new JPopupMenu();
        final boolean isParentOrChild = model.isParentOrChild(head, tail);
        final boolean areMarried = model.areMarried(head, tail);
        final boolean canMarry = model.isNotMarried(head)
                && model.isNotMarried(tail)
                && !isParentOrChild;
        if ( canMarry )
        {
            final @NotNull JMenuItem jmiMarry = new JMenuItem("Marry To...");
            jmiMarry.addActionListener(new MarryToPopupListener(head, tail));
            popupMenu.add(jmiMarry);
        }
        else
        {
            if ( areMarried )
            {
                final @NotNull JMenuItem jmiDivorce = new JMenuItem("Divorce");
                jmiDivorce.addActionListener( new DivorcePopupListener(head, tail) );
                popupMenu.add(jmiDivorce);
            }
        }
        //
        final boolean canParent = !isParentOrChild && !areMarried;
        if ( canParent )
        {
            final @NotNull JMenuItem jmiSetParent = new JMenuItem("Create Parent -> Child Bond");
            final @NotNull JMenuItem jmiSetChild = new JMenuItem("Create Child -> Parent Bond");
            jmiSetParent.addActionListener(new BegetPopupListener(head, tail));
            jmiSetChild.addActionListener(new BegetPopupListener(tail, head));
            popupMenu.add(jmiSetChild);
            popupMenu.add(jmiSetParent);
        }
        else
        {
            if ( isParentOrChild )
            {
                final @NotNull JMenuItem jmiDropParent = new JMenuItem("Remove Parentship");
                jmiDropParent.addActionListener(new RemoveParentshipListener(head, tail));
                popupMenu.add(jmiDropParent);
            }
        }
        // Show popup menu
        popupMenu.show(this, x, y);
    }

    /*
        Returns the node which contains the (cursorAbs, cursorOrd) point.
     */
    private @Nullable Person detectNode(final int cursorAbs, final int cursorOrd)
    {
        @Nullable Person resultPerson = null;
        for (Person person : model.getPeople())
        {
            final double distanceBetweenPoints = camera.d(
                    camera.toRealX(cursorAbs),
                    camera.toRealY(cursorOrd),
                    person.getX() + DEFAULT_CIRCLE_RADIUS,
                    person.getY() + DEFAULT_CIRCLE_RADIUS
            );
            if ( distanceBetweenPoints <= DEFAULT_CIRCLE_RADIUS )
            {
                resultPerson = person;
                break;
            }
        }
        return resultPerson;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        final @NotNull Graphics2D g2d = (Graphics2D)g;
        g2d.setBackground(BACKGROUND_COLOR);
        // Set antialiasing options for shapes
        final @NotNull Object shapeAntialiasing;
        if ( MainFrame.graphicsOptions.get(MainFrame.SHAPE_ANTIALIASING) )
        {
            shapeAntialiasing = RenderingHints.VALUE_ANTIALIAS_ON;
        }
        else
        {
            shapeAntialiasing = RenderingHints.VALUE_ANTIALIAS_OFF;
        }
        // Set antialiasing options for text
        final @NotNull Object textAntialiasing;
        if ( MainFrame.graphicsOptions.get(MainFrame.TEXT_ANTIALIASING) )
        {
            textAntialiasing = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
        }
        else
        {
            textAntialiasing = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        }
        // Set the options
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, shapeAntialiasing);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textAntialiasing);
        // Paint all nodes and entity between them
        model.getBonds().forEach(bond -> bond.draw(this, g2d));
        model.getPeople().forEach(node -> node.draw(this, g2d));
    }

    @NotNull JFrame getParentFrame()
    {
        return (JFrame)SwingUtilities.getWindowAncestor(this);
    }

    public int getAnimationCount()
    {
        return increment.getAnimationCount();
    }

    public @NotNull Camera getCamera()
    {
        return camera;
    }

    @NotNull Ontology getModel()
    {
        return model;
    }

    void centerOnPerson(final @NotNull Person p)
    {
        camera.center(
                camera.toScreenX( p.getX() + DEFAULT_CIRCLE_RADIUS ),
                camera.toScreenY( p.getY() + DEFAULT_CIRCLE_RADIUS )
        );
    }



    private class CanvasMouseListener extends MouseAdapter
    {
        final static int LEFT_BUTTON = 1;
        final static int MIDDLE_BUTTON = 2;
        final static int RIGHT_BUTTON = 3;
        private int lastRelativeX;  // Stores the X coordinate of the last position of the dragged mouse
        private int lastRelativeY;  // Stores the Y coordinate of the last position of the dragged mouse
        private @Nullable Person lastPressedPerson; // Stores the last node on which the user pressed the mouse button
        private @Nullable Person lastSelectedPerson; // Stores the last node the user selected (clicked once)

        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);
            // Only left button clicks are allowed
            if ( e.getButton() != LEFT_BUTTON )
            {
                return;
            }
            // Only single and double left clicks are supported
            final int clickCount = e.getClickCount();
            if ( clickCount == 1 )
            {
                if ( lastPressedPerson == null )
                {
                    if ( lastSelectedPerson != null )
                    {
                        // User wants to clear last selected node
                        lastSelectedPerson.deselect();
                        lastSelectedPerson = null;
                    }
                }
                else
                {
                    if ( lastSelectedPerson == null )
                    {
                        // User wants to select the first node
                        lastSelectedPerson = lastPressedPerson;
                        lastSelectedPerson.select();
                    }
                    else
                    {
                        // User already selected a node and now selects another node
                        // or deselect the last selected node
                        if ( !lastSelectedPerson.equals(lastPressedPerson) )
                        {
                            lastSelectedPerson.deselect();
                            lastPressedPerson.select();
                            lastSelectedPerson = lastPressedPerson;
                        }
                        else
                        {
                            lastSelectedPerson.deselect();
                            lastSelectedPerson = null;
                        }
                    }
                }
            }
            else if ( clickCount == 2 )
            {
                if ( lastSelectedPerson != null )
                {
                    lastSelectedPerson.deselect();
                }
                lastSelectedPerson = null;
                //
                if ( lastPressedPerson == null )
                {
                    // User double-clicked on empty place, so he wants to create a new node here
                    profileFrame.createNewProfile(camera.toRealX(e.getX()), camera.toRealY(e.getY()));
                }
                else
                {
                    // User double-clicked on a node, so he wants to see its profile
                    profileFrame.updateProfile(lastPressedPerson);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            super.mousePressed(e);
            //
            if ( (e.getButton() == RIGHT_BUTTON) )
            {
                lastPressedPerson = detectNode(e.getX(), e.getY());
                //
                if (e.isPopupTrigger())
                {
                    final boolean bondMenuTriggered = (lastSelectedPerson != null)
                            && (lastPressedPerson != null)
                            && !(lastPressedPerson.equals(lastSelectedPerson));
                    if (bondMenuTriggered)
                    {
                        showBondPopupMenu(lastSelectedPerson, lastPressedPerson, e.getX(), e.getY());
                    }
                    else
                    {
                        showNodePopupMenu( e.getX(), e.getY() );
                    }
                }
            }
            else if ( e.getButton() == LEFT_BUTTON )
            {
                lastPressedPerson = detectNode(e.getX(), e.getY());
            }
            //
            saveCurrentMousePosition(e.getX(), e.getY());
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            super.mouseDragged(e);
            //
            final int buttonId = e.getButton();
            if (buttonId == LEFT_BUTTON)
            {
                if (lastPressedPerson != null)
                {
                    final double offsetX = camera.toRealX(e.getX()) - camera.toRealX(lastRelativeX);
                    final double offsetY = camera.toRealY(e.getY()) - camera.toRealY(lastRelativeY);
                    lastPressedPerson.move(offsetX, offsetY);
                    repaint();
                }
            }
            else if (buttonId == MIDDLE_BUTTON)
            {
                camera.moveCanvas(e.getX() - lastRelativeX, e.getY() - lastRelativeY);
            }
            saveCurrentMousePosition(e.getX(), e.getY());
        }

        private void saveCurrentMousePosition(final int x, final int y)
        {
            this.lastRelativeX = x;
            this.lastRelativeY = y;
        }
    }

    private class CanvasMouseWheelListener implements MouseWheelListener
    {
        private final double oneClickScaleFactor = 1.03;

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            final int nClicks = e.getWheelRotation();
            final double scaleFactor = (nClicks > 0)? 1 / oneClickScaleFactor : oneClickScaleFactor;
            camera.scaleOnScreenPoint( scaleFactor, e.getX(), e.getY() );
        }
    }

    private class IncrementTask extends TimerTask
    {
        private int animationCount = 0;

        @Override
        public void run()
        {
            if ( MainFrame.graphicsOptions.get(MainFrame.ANIMATION) )
            {
                animationCount += 1;
                if (animationCount == Integer.MAX_VALUE)
                {
                    animationCount = 0;
                }
                repaint();
            }
        }

        private int getAnimationCount()
        {
            return animationCount;
        }
    }

    private class AddPersonPopupListener implements ActionListener
    {
        private final int newNodePositionX;
        private final int newNodePositionY;

        AddPersonPopupListener(final int newNodePositionX, final int newNodePositionY)
        {
            this.newNodePositionX = newNodePositionX;
            this.newNodePositionY = newNodePositionY;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            profileFrame.createNewProfile(camera.toRealX(newNodePositionX), camera.toRealY(newNodePositionY));
        }
    }

    private class RemovePersonPopupListener implements ActionListener
    {
        private final @NotNull Person personToRemove;

        RemovePersonPopupListener(final @NotNull Person personToRemove)
        {
            this.personToRemove = personToRemove;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final String message = String.format("Do you really want to expunge %s %s and all relations to %s?",
                    personToRemove.getFirstName(),
                    personToRemove.getLastName(),
                    ( personToRemove.isMale() )? "him" : "her"
            );
            final int choice = JOptionPane.showConfirmDialog(null,
                    message,
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION)
            {
                model.removePerson(personToRemove);
            }
        }
    }

    private class MarryToPopupListener implements ActionListener
    {
        private final @NotNull Person firstSpouse;
        private final @NotNull Person secondSpouse;

        MarryToPopupListener(final @NotNull Person firstSpouse, final @NotNull Person secondSpouse)
        {
            this.firstSpouse = firstSpouse;
            this.secondSpouse = secondSpouse;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final String text = String.format("Do you bless the marriage between %s %s and %s %s?\n" +
                            "If yes, then enter the date of marriage in format dd.mm.year, otherwise press cancel.",
                    firstSpouse.getFirstName(),
                    firstSpouse.getLastName(),
                    secondSpouse.getFirstName(),
                    secondSpouse.getLastName()
            );
            String dateOfMarriage = "";
            String caption = "Enter date of marriage";
            while ( (dateOfMarriage != null) && !dateOfMarriage.matches(TDate.DATE_PATTERN) )
            {
                dateOfMarriage = JOptionPane.showInputDialog(GenealogyView.this,
                        text,
                        caption,
                        JOptionPane.INFORMATION_MESSAGE
                );
                caption = "Wrong date format";
            }
            if ( dateOfMarriage != null )
            {
                model.marry(firstSpouse, secondSpouse, dateOfMarriage);
            }
        }
    }

    private class DivorcePopupListener implements ActionListener
    {
        private final @NotNull Person firstSpouse;
        private final @NotNull Person secondSpouse;

        DivorcePopupListener(final @NotNull Person firstSpouse, final @NotNull Person secondSpouse)
        {
            this.firstSpouse = firstSpouse;
            this.secondSpouse = secondSpouse;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final String message = String.format(
                    "Do you really want to divorce %s %s and %s %s?",
                    firstSpouse.getFirstName(),
                    firstSpouse.getLastName(),
                    secondSpouse.getFirstName(),
                    secondSpouse.getLastName()
            );
            final int choice = JOptionPane.showConfirmDialog(GenealogyView.this,
                    message,
                    "Confirm Divorce",
                    JOptionPane.YES_NO_OPTION
            );
            if ( choice == JOptionPane.YES_OPTION )
            {
               model.divorce(firstSpouse, secondSpouse);
            }
        }
    }

    private class BegetPopupListener implements ActionListener
    {
        private final @NotNull Person parent;
        private final @NotNull Person child;

        BegetPopupListener(final @NotNull Person parent, final @NotNull Person child)
        {
            this.parent = parent;
            this.child = child;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final String message = String.format("Set %s %s as a parent of %s %s?",
                    parent.getFirstName(),
                    parent.getLastName(),
                    child.getFirstName(),
                    child.getLastName()
            );
            final int choice = JOptionPane.showConfirmDialog(GenealogyView.this,
                    message,
                    "Confirm Parentship",
                    JOptionPane.YES_NO_OPTION
            );
            if ( choice == JOptionPane.YES_OPTION )
            {
                // Add new parental bond
                final @NotNull Bond parentalBond = new Bond(parent, child);
                model.addBond(parentalBond);
                // Insert new parental row in the table `beget`
                final String query = String.format(
                        "INSERT INTO beget(parent_id, child_id) VALUES (%d, %d)",
                        parent.getID(),
                        child.getID()
                );
                Database.instance.issueSQL(query);
            }
        }
    }

    private class RemoveParentshipListener implements ActionListener
    {
        private final @NotNull Person firstPerson;
        private final @NotNull Person secondPerson;

        RemoveParentshipListener(final @NotNull Person firstPerson, final @NotNull Person secondPerson)
        {
            this.firstPerson = firstPerson;
            this.secondPerson = secondPerson;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final String message = String.format(
                    "Do you really want to remove the parentship between %s %s and %s %s?",
                    firstPerson.getFirstName(),
                    firstPerson.getLastName(),
                    secondPerson.getFirstName(),
                    secondPerson.getLastName()
            );
            final int choice = JOptionPane.showConfirmDialog(
                    GenealogyView.this,
                    message,
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION
            );
            if ( choice == JOptionPane.YES_OPTION )
            {
                model.removeParentship(firstPerson, secondPerson);
            }
        }
    }
}