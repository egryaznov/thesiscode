package foundation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

class GenealogyPresenter
{
    private final @NotNull GenealogyModel model;
    private final @NotNull GenealogyView view;

    GenealogyPresenter(final @NotNull GenealogyView view)
    {
        this.view = view;
        this.model = new GenealogyModel();
        // Register listeners
        final @NotNull MainFrameMouseListener mouseListener = new MainFrameMouseListener();
        this.view.addMouseListener( mouseListener );
        this.view.addMouseMotionListener( mouseListener );
        this.view.addMouseWheelListener( new MainFrameMouseWheelListener() );
        // Elicit and construct nodes and bonds from the model
        prepareNodes();
        prepareBonds();
        view.repaint();
    }

    private void prepareNodes()
    {
        final String queryAllNodes = "SELECT person.id, photo.path, photo.x, photo.y, person.sex " +
                                        "FROM person, has, photo " +
                                            "WHERE person.id = has.person_id AND has.photo_id = photo.id";
        try ( final @Nullable ResultSet nodesTable = model.executeQuery(queryAllNodes) )
        {
            if (nodesTable == null)
            {
                System.out.println("Cannot properly load nodes");
                return;
            }
            //
            while (nodesTable.next())
            {
                final int person_id = nodesTable.getInt(1);
                final String photo_path = nodesTable.getString(2);
                final int x = nodesTable.getInt(3);
                final int y = nodesTable.getInt(4);
                final int sex = nodesTable.getInt(5);
                final Color node_color = (sex == GenealogyModel.MALE)? Color.CYAN : Color.PINK;
                @NotNull Image photo;
                // Creating new node...
                try
                {
                    photo = ImageIO.read(new File(photo_path));
                } catch (IOException e)
                {
                    System.out.println("Cannot load photo " + photo_path);
                    photo = (new Canvas()).createImage(70, 70); // Stub blank photo
                    e.printStackTrace();
                }
                view.addNode( new Node(person_id, x, y, photo, node_color) );
            }
        } catch (SQLException e)
        {
            System.out.println("Oups");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void prepareBonds()
    {
        final String queryWed = "SELECT first_spouse_id, second_spouse_id FROM wed";
        final String queryBeget = "SELECT * FROM beget";
        try ( final @Nullable ResultSet wedTable = model.executeQuery(queryWed);
                final @Nullable ResultSet begetTable = model.executeQuery(queryBeget))
        {
            if ((wedTable == null) || (begetTable == null))
            {
                System.out.println("Cannot properly load bonds (wed or beget) from the DB");
                return;
            }
            while (wedTable.next())
            {
                view.addBond(wedTable.getInt(1), wedTable.getInt(2), true);
            }
            while (begetTable.next())
            {
                view.addBond(begetTable.getInt(1), begetTable.getInt(2), false);
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private class MainFrameMouseListener extends MouseAdapter
    {
        final static int LEFT_BUTTON = 1;
        final static int MIDDLE_BUTTON = 2;
        final static int RIGHT_BUTTON = 3;
        private int lastRelativeX;  // Stores the X coordinate of the last position of the dragged mouse
        private int lastRelativeY;  // Stores the Y coordinate of the last position of the dragged mouse
        @Nullable Node nodeUnderCursor;

        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);
            if (nodeUnderCursor != null)
            {
                nodeUnderCursor.toggle();
                nodeUnderCursor = null;
                view.repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            super.mousePressed(e);
            final int buttonId = e.getButton();
            if ( (buttonId == LEFT_BUTTON) || (buttonId == MIDDLE_BUTTON) )
            {
                if (buttonId == LEFT_BUTTON)
                {
                    nodeUnderCursor = view.detectNode(e.getX(), e.getY());
                }
                saveCurrentMousePosition(e.getX(), e.getY());
            }
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            super.mouseDragged(e);
            final int buttonId = e.getButton();
            if ( (buttonId == LEFT_BUTTON) || (buttonId == MIDDLE_BUTTON) )
            {
                if (buttonId == LEFT_BUTTON)
                {
                    if (nodeUnderCursor != null)
                    {
                        view.moveNode(nodeUnderCursor, e.getX() - lastRelativeX, e.getY() - lastRelativeY);
                    }
                }
                else
                {
                    view.moveCanvas(e.getX() - lastRelativeX, e.getY() - lastRelativeY);
                }
                saveCurrentMousePosition(e.getX(), e.getY());
            }
        }

        private void saveCurrentMousePosition(final int x, final int y)
        {
            this.lastRelativeX = x;
            this.lastRelativeY = y;
        }
    }

    private class MainFrameMouseWheelListener implements MouseWheelListener
    {
        private final double oneClickScaleFactor = 1.03;

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            final int nClicks = e.getWheelRotation();
            final double scaleFactor = (nClicks < 0)? 1 / oneClickScaleFactor : oneClickScaleFactor;
            view.scaleCanvas( scaleFactor, e.getX(), e.getY() );
        }
    }
}
