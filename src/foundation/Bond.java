package foundation;

import org.jetbrains.annotations.NotNull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class Bond
{
    private final @NotNull Node firstNode;
    private final @NotNull Node secondNode;

    Bond(final @NotNull Node firstNode, final @NotNull Node secondNode)
    {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
    }



    public @NotNull Color getColor()
    {
        return secondNode.getColor();
    }

    /*
        Checks if the bond is linked to the specified `node`
     */
    public boolean isLinkedTo(final @NotNull Node node)
    {
        return isHead(node) || isTail(node);
    }

    private boolean isHead(final @NotNull Node node)
    {
        return firstNode.equals(node);
    }

    private boolean isTail(final @NotNull Node node)
    {
        return secondNode.equals(node);
    }

    public boolean isBetween(final @NotNull Node head, final @NotNull Node tail)
    {
        return isHead(head) && isTail(tail);
    }

    public @NotNull Node getHead()
    {
        return firstNode;
    }

    public @NotNull Node getTail()
    {
        return secondNode;
    }

    public void draw(final @NotNull GenealogyView view, final @NotNull Graphics2D g2d)
    {
        g2d.setColor( getColor() );
        final @NotNull Camera camera = view.getCamera();
        final int x0 = camera.toScreenX(firstNode.getX() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
        final int y0 = camera.toScreenY(firstNode.getY() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
        final int x1 = camera.toScreenX(secondNode.getX() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
        final int y1 = camera.toScreenY(secondNode.getY() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
        g2d.setStroke( new BasicStroke((float)camera.scale(2.0f)) );
        g2d.drawLine(x0, y0, x1, y1);
        // Draw a small circle that will show the direction of this bond
        final double x2 = getHead().getX() + GenealogyView.DEFAULT_CIRCLE_RADIUS;
        final double y2 = getHead().getY() + GenealogyView.DEFAULT_CIRCLE_RADIUS;
        final double x3 = getTail().getX() + GenealogyView.DEFAULT_CIRCLE_RADIUS;
        final double y3 = getTail().getY() + GenealogyView.DEFAULT_CIRCLE_RADIUS;
        final double length = camera.d(x2, y2, x3, y3);
        final double nLineSegment = 50;
        final double step = length / nLineSegment;
        final double p = length / (step * (view.getAnimationCount() % nLineSegment));
        final double radius = 3;
        final int kX = ( Math.max(x2, x3) == x2 )? -1 : 1;
        final int kY = ( Math.max(y2, y3) == y2 )? -1 : 1;
        final double originX = x2 + kX * ( Math.abs(x3 - x2) ) / p - radius;
        final double originY = y2 + kY * ( Math.abs(y3 - y2) ) / p - radius;
        g2d.setStroke( new BasicStroke((float)camera.scale(2.0f)) );
        g2d.fill(new Ellipse2D.Double(
                camera.toScreenX(originX),
                camera.toScreenY(originY),
                camera.scale(2 * radius),
                camera.scale(2 * radius))
        );
    }

    @Override
    public boolean equals(Object obj)
    {
        return (obj instanceof Bond)
                && (firstNode.equals(((Bond) obj).firstNode))
                && (secondNode.equals(((Bond) obj).secondNode));
    }
}