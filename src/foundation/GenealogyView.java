package foundation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

class GenealogyView extends JPanel
{
    private static final int DEFAULT_CIRCLE_DIAMETER = 70;
    private static final int DEFAULT_CIRCLE_RADIUS = DEFAULT_CIRCLE_DIAMETER / 2;
    private int offsetX;
    private int offsetY;
    private double scaleOriginX;
    private double scaleOriginY;
    private double scaleFactor = 1;
    private final @NotNull ArrayList<Node> nodes = new ArrayList<>();
    private final @NotNull ArrayList<Bond> bonds = new ArrayList<>();

    GenealogyView()
    {
        this.setBackground(Color.GRAY);
    }

    public void moveCanvas(final int offsetX, final int offsetY)
    {
        this.offsetX += offsetX;
        this.offsetY += offsetY;
        repaint();
    }

    private void moveCanvasToPoint(final int abscissa, final int ordinate)
    {
        this.offsetX = abscissa;
        this.offsetY = ordinate;
    }

    public void scaleCanvas(final double scaleFactor, final int x0, final int y0)
    {
        final double minScaleFactor = 0.5;
        final double maxScaleFactor = 5;
        //
        this.scaleFactor *= scaleFactor;
        if (this.scaleFactor < minScaleFactor)
        {
            this.scaleFactor = minScaleFactor;
        }
        else if (this.scaleFactor > maxScaleFactor)
        {
            this.scaleFactor = maxScaleFactor;
        }
        // moveCanvasToPoint(projectX(x0), projectY(y0));
        this.scaleOriginX = inverseProjectX(x0);
        this.scaleOriginY = inverseProjectY(y0);
        //
        repaint();
    }

    public void dropBond(final @NotNull Node a, final @NotNull Node b)
    {
        // Remove if a bond consists of nodes `a` and `b`
        bonds.removeIf( bond ->
                (bond.getFirstNode().equals(a) || bond.getFirstNode().equals(b))
                        && (bond.getSecondNode().equals(a) || bond.getSecondNode().equals(b))
        );
    }

    public void addBond(final int first_node_id, final int second_node_id, final boolean marital)
    {
        @Nullable Node firstNode = null;
        @Nullable Node secondNode = null;
        for (Node node : nodes)
        {
            if (node.getId() == first_node_id)
            {
                firstNode = node;
            }
            else if (node.getId() == second_node_id)
            {
                secondNode = node;
            }
        }
        //
        if ((firstNode == null) || (secondNode == null))
        {
            System.out.println("Fatal Error! Cannot find nodes to bond, exiting...");
            System.exit(1);
        }
        final @NotNull Color color = (marital)? Color.ORANGE : secondNode.getBorderColor();
        bonds.add( new Bond(firstNode, secondNode, color) );
    }

    public void addNode(final @NotNull Node node)
    {
        nodes.add(node);
    }

    public void dropNode(final int photoId)
    {
        nodes.removeIf( p -> (p.getId() == photoId) );
    }

    private double inverseProjectX(final double x)
    {
        return ( x - offsetX - this.scaleOriginX * (1 - this.scaleFactor) ) / this.scaleFactor;
    }

    private double inverseProjectY(final double y)
    {
        return ( y - offsetY - this.scaleOriginY * (1 - this.scaleFactor) ) / this.scaleFactor;
    }

    private int projectX(final double x)
    {
        return (int) Math.ceil( this.scaleFactor * x + this.scaleOriginX * (1 - this.scaleFactor) + offsetX );
    }

    private int projectY(final double y)
    {
        return (int) Math.ceil( this.scaleFactor * y + this.scaleOriginY * (1 - this.scaleFactor) + offsetY );
    }

    private int scale(final double length)
    {
        return (int) Math.ceil( this.scaleFactor * length );
    }

    private void drawNode(final @NotNull Graphics2D g2d, final @NotNull Node node)
    {
        g2d.setClip(
                new Ellipse2D.Float(projectX(node.getX()),
                projectY(node.getY()),
                scale(DEFAULT_CIRCLE_DIAMETER),
                scale(DEFAULT_CIRCLE_DIAMETER))
        );
        g2d.drawImage(
                node.getPhoto(),
                projectX(node.getX()),
                projectY(node.getY()),
                scale(DEFAULT_CIRCLE_DIAMETER),
                scale(DEFAULT_CIRCLE_DIAMETER),
                null
        );
        // Remove the clip from the graphics
        g2d.setClip(null);
        // Draw a circled border around the photo
        final @NotNull Color borderColor = (node.isSelected())? Color.RED : node.getBorderColor();
        g2d.setColor(borderColor);
        g2d.drawOval(
                projectX(node.getX()),
                projectY(node.getY()),
                scale(DEFAULT_CIRCLE_DIAMETER),
                scale(DEFAULT_CIRCLE_DIAMETER)
        );
    }

    private void drawBond(final @NotNull Graphics2D g2d, final @NotNull Bond bond)
    {
        g2d.setColor(bond.getColor());

        final int x0 = bond.getFirstNode().getX() + scale(DEFAULT_CIRCLE_RADIUS);
        final int y0 = bond.getFirstNode().getY() + scale(DEFAULT_CIRCLE_RADIUS);
        final int x1 = bond.getSecondNode().getX() + scale(DEFAULT_CIRCLE_RADIUS);
        final int y1 = bond.getSecondNode().getY() + scale(DEFAULT_CIRCLE_RADIUS);
        g2d.drawLine(projectX(x0), projectY(y0), projectX(x1), projectY(y1));
    }

    public @Nullable Node detectNode(final int x, final int y)
    {
        @Nullable Node resultNode = null;
        for (Node node : nodes)
        {
            final int x0 = node.getX() + scale(DEFAULT_CIRCLE_RADIUS);
            final int y0 = node.getY() + scale(DEFAULT_CIRCLE_RADIUS);
            if (d(x, y, projectX(x0), projectY(y0)) <= scale(DEFAULT_CIRCLE_RADIUS))
            {
                resultNode = node;
                break;
            }
        }
        return resultNode;
    }

    public void moveNode(final @NotNull Node node, final int offsetX, final int offsetY)
    {
        node.setX(node.getX() + offsetX);
        node.setY(node.getY() + offsetY);
        repaint();
    }

    /*
        Calculated a Euclidean distance between two points (scaleOriginX, scaleOriginY) and (x1, y1).
     */
    private double d(final int x0, final int y0, final int x1, final int y1)
    {
        return Math.sqrt( (x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0) );
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        final @NotNull Graphics2D g2d = (Graphics2D)g;
        // Enable AntiAliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Paint all nodes and bonds between them
        bonds.forEach(bond -> drawBond(g2d, bond));
        nodes.forEach(node -> drawNode(g2d, node));
        //
        // g2d.setColor(Color.BLUE);
        // g2d.fillRect( projectX(100), projectY(100), scale(20), scale(20) );
        // g2d.fill(new Ellipse2D.Double( projectX(300), projectY(300), scale(30), scale(30) ));
        // g2d.drawLine(projectX(0), projectY(getHeight() / 2), projectX(getWidth()), projectY(getHeight() / 2));
        // g2d.drawLine(projectX(getWidth() / 2), projectY(0), projectX(getWidth() / 2), projectY(getHeight()));
        // g2d.setColor(Color.RED);
        // g2d.fillRect( this.scaleOriginX, this.scaleOriginY, 6, 6 );
    }
}