package foundation.entity;

import foundation.main.Camera;
import foundation.main.GenealogyView;
import org.jetbrains.annotations.NotNull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class MaritalBond extends Bond
{
    private static final @NotNull Color MARITAL_BOND_COLOR = new Color(255, 128, 0);
    private final String dateOfWedding;

    public MaritalBond(final @NotNull Person firstPerson, final @NotNull Person secondPerson, final String dateOfWedding)
    {
        super(firstPerson, secondPerson);
        this.dateOfWedding = dateOfWedding;
    }



    @Override
    public void draw(@NotNull GenealogyView view, @NotNull Graphics2D g2d)
    {
        // There are two marital entity between those nodes, since the marital relation is symmetric
        // So draw the bond between two nodes only once
        if (getHead().getID() < getTail().getID())
        {
            g2d.setColor( getColor() );
            final @NotNull Camera camera = view.getCamera();
            final int x0 = camera.toScreenX(getHead().getX() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
            final int y0 = camera.toScreenY(getHead().getY() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
            final int x1 = camera.toScreenX(getTail().getX() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
            final int y1 = camera.toScreenY(getTail().getY() + GenealogyView.DEFAULT_CIRCLE_RADIUS);
            final @NotNull Stroke maritalStroke = new BasicStroke(
                    (float)camera.scale(2.0f),
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    1.0f,
                    new float[] {10.0f},
                    (view.getAnimationCount() % 10)
            );
            g2d.setStroke( maritalStroke );
            g2d.drawLine(x0, y0, x1, y1);
        }
    }

    public String getDateOfWedding()
    {
        return dateOfWedding;
    }

    @Override
    @NotNull Color getColor()
    {
        return MARITAL_BOND_COLOR;
    }
}
