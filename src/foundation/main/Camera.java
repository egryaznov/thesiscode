package foundation.main;

import org.jetbrains.annotations.NotNull;

public class Camera
{
    private double originX;
    private double originY;
    private double zoomedPointX;
    private double zoomedPointY;
    private double scaleFactor = 1;
    private final @NotNull GenealogyView view;

    Camera(final @NotNull GenealogyView view)
    {
        this.view = view;
    }



    public void moveCanvas(final double offsetX, final double offsetY)
    {
        this.originX -= offsetX;
        this.originY -= offsetY;
        view.repaint();
    }

    /*
        Moves camera such that the real point (`realAbs`, `realOrd`) corresponds exactly to the screen point
        (`screenAbs`, `screenOrd`).
     */
    private void equate(final int screenAbs, final int screenOrd, final double realAbs, final double realOrd)
    {
        originX += toScreenX(realAbs) - screenAbs;
        originY += toScreenY(realOrd) - screenOrd;
    }

    /*
        Zooms in the camera at exactly the (`x0`, `y0`) point.
     */
    public void scaleOnScreenPoint(final double scaleFactor, final int x0, final int y0)
    {
        final double minScaleFactor = 0.5;
        final double maxScaleFactor = 5;
        //
        zoomedPointX = toRealX(x0);
        zoomedPointY = toRealY(y0);
        this.scaleFactor *= scaleFactor;
        if (this.scaleFactor < minScaleFactor)
        {
            this.scaleFactor = minScaleFactor;
        }
        else if (this.scaleFactor > maxScaleFactor)
        {
            this.scaleFactor = maxScaleFactor;
        }
        equate(x0, y0, zoomedPointX, zoomedPointY);
        view.repaint();
    }

    /*
        Converts the abscissa `x` of a real point to the abscissa of a screen point.
     */
    public int toScreenX(final double x)
    {
        return toInt( x * scaleFactor - (scaleFactor - 1) * zoomedPointX - originX );
    }

    /*
        Converts the ordinate `y` of a real point to the ordinate of a screen point.
     */
    public int toScreenY(final double y)
    {
        return toInt( y * scaleFactor - (scaleFactor - 1) * zoomedPointY - originY );
    }

    /*
        Converts the abscissa `x` of a screen point to the abscissa of a real point.
     */
    public double toRealX(final int x)
    {
        return (x + (scaleFactor - 1) * zoomedPointX + originX) / scaleFactor;
    }

    /*
        Converts the ordinate `x` of a screen point to the ordinate of a real point.
     */
    public double toRealY(final int y)
    {
        return (y + (scaleFactor - 1) * zoomedPointY + originY) / scaleFactor;
    }

    public double scale(final double length)
    {
        return this.scaleFactor * length;
    }

    public static int toInt(final double realNumber)
    {
        return (int)Math.ceil(realNumber);
    }

    /*
        Calculated a Euclidean distance between two points (x0, y0) and (x1, y1).
     */
    public double d(final double x0, final double y0, final double x1, final double y1)
    {
        return Math.sqrt( (x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0) );
    }
}
