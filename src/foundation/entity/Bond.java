package foundation.entity;

import foundation.main.Camera;
import foundation.main.GenealogyView;
import org.jetbrains.annotations.NotNull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class Bond
{
    private final @NotNull Person firstPerson;
    private final @NotNull Person secondPerson;

    public Bond(final @NotNull Person firstPerson, final @NotNull Person secondPerson)
    {
        this.firstPerson = firstPerson;
        this.secondPerson = secondPerson;
    }



    public @NotNull Color getColor()
    {
        return secondPerson.getColor();
    }

    /*
        Checks if the bond is linked to the specified `getPerson`
     */
    public boolean isLinkedTo(final @NotNull Person person)
    {
        return isHead(person) || isTail(person);
    }

    private boolean isHead(final @NotNull Person person)
    {
        return firstPerson.equals(person);
    }

    private boolean isTail(final @NotNull Person person)
    {
        return secondPerson.equals(person);
    }

    public boolean isBetween(final @NotNull Person head, final @NotNull Person tail)
    {
        return isHead(head) && isTail(tail);
    }

    public @NotNull Person getHead()
    {
        return firstPerson;
    }

    public @NotNull Person getTail()
    {
        return secondPerson;
    }

    public void draw(final @NotNull GenealogyView view, final @NotNull Graphics2D g2d)
    {
        g2d.setColor( getColor() );
        final @NotNull Camera camera = view.getCamera();
        final int x0 = camera.toScreenX(firstPerson.getX() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
        final int y0 = camera.toScreenY(firstPerson.getY() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
        final int x1 = camera.toScreenX(secondPerson.getX() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
        final int y1 = camera.toScreenY(secondPerson.getY() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
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
                && (firstPerson.equals(((Bond) obj).firstPerson))
                && (secondPerson.equals(((Bond) obj).secondPerson));
    }
}