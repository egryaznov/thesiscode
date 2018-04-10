package foundation.main;

import org.jetbrains.annotations.NotNull;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Camera
{
    private double originX;
    private double originY;
    private double zoomedPointX;
    private double zoomedPointY;
    private double scaleFactor = 1;
    private final @NotNull GenealogyView view;
    private final @NotNull SmoothCenteringOnPoint action;
    private final @NotNull Timer movingTimer;

    Camera(final @NotNull GenealogyView view)
    {
        this.view = view;
        this.action = new SmoothCenteringOnPoint();
        this.movingTimer = new Timer(1000 / SmoothCenteringOnPoint.FPS, action);
        movingTimer.setInitialDelay(0);
    }



    void moveCanvas(final double offsetX, final double offsetY)
    {
        this.originX -= offsetX;
        this.originY -= offsetY;
        view.repaint();
    }

    /*
        Continuously moves the camera to the real point (`realAbsTo`, `realOrdTo`) so that, when stopped,
        this point becomes the center. So this method is akin to `equate`, but the latter move the camera abruptly,
        with one giant discrete oneStepLen. In contrast, this method does the same, but smoothly and slowly, so a user can
        get a sense of direction.
     */
    void center(final double x, final double y)
    {
        // Disable canvas panel during the maneuver
        // view.setEnabled(false);
        // Stop another movement if in progress
        if (movingTimer.isRunning())
        {
            movingTimer.stop();
        }
        //
        action.setRealAbsTo(x);
        action.setRealOrdTo(y);
        System.out.println("Starting the movement");
        movingTimer.restart();
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
    void scaleOnScreenPoint(final double scaleFactor, final int x0, final int y0)
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
    double toRealX(final int x)
    {
        return (x + (scaleFactor - 1) * zoomedPointX + originX) / scaleFactor;
    }

    /*
        Converts the ordinate `x` of a screen point to the ordinate of a real point.
     */
    double toRealY(final int y)
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
        Calculated a Euclidean distance between two real points (x0, y0) and (x1, y1).
     */
    public double d(final double x0, final double y0, final double x1, final double y1)
    {
        return Math.sqrt( (x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0) );
    }



    private class SmoothCenteringOnPoint implements ActionListener
    {
        private       double   rScale;
        private       double   realAbsTo        = 0;
        private       double   realOrdTo        = 0;
        static  final int      FPS              = 60;
        private final int      screenMiddleAbs  = GenealogyView.DEFAULT_WIDTH / 2;
        private final int      screenMiddleOrd  = GenealogyView.DEFAULT_HEIGHT / 2;
        private final double   acceptedDistance = 2;
        private final int      numberOfSteps    = 500;
        private final double   oneStep          = 0.1;
        private       double[] nextPoint        = { toRealX(screenMiddleAbs), toRealY(screenMiddleOrd) };

        SmoothCenteringOnPoint()
        {
            this.rScale = oneStep * d(toRealX(screenMiddleAbs), toRealY(screenMiddleOrd), realAbsTo, realOrdTo) / numberOfSteps;
        }

        void setRealAbsTo(final double realAbsTo)
        {
            this.realAbsTo = realAbsTo;
            this.rScale = oneStep * d(toRealX(screenMiddleAbs), toRealY(screenMiddleOrd), realAbsTo, realOrdTo) / numberOfSteps;
        }

        void setRealOrdTo(final double realOrdTo)
        {
            this.realOrdTo = realOrdTo;
            this.rScale = oneStep * d(nextPoint[0], nextPoint[1], realAbsTo, realOrdTo) / numberOfSteps;
        }

        /*
            Continuously moves the camera to the real point (`realAbsTo`, `realOrdTo`) so that, when stopped,
            this point becomes the center. So this method is akin to `equate`, but the latter move the camera abruptly,
            with one giant discrete oneStepLen. In contrast, this method does the same, but smoothly and slowly, so a user can
            get a sense of direction.
         */
        void center()
        {
            // Calculate the next point
            final double x = nextPoint[0];
            final double y = nextPoint[1];
            nextPoint = propel(x, y, realAbsTo, realOrdTo);
            // Move the canvas
            equate(screenMiddleAbs, screenMiddleOrd, nextPoint[0], nextPoint[1]);
            view.repaint();
        }

        /*
            Imagine a segment of the line constructed by two points, (`startAbs`, `startOrd`) and (`finishAbs`, `finishOrd`),
            with the left end being (`startAbs`, `startOrd`) and the right end being (`finishAbs`, `finishOrd`).
            Now imagine moving, by the line, the left end of this segment by exactly `oneStepLen` "meters" towards the right end.
            We will get a new, offset point: (x, y). This method calculates and returns this (x, y) point. Used only once in
            the method `center`.
         */
        private double[] propel(final double startAbs, final double startOrd, final double finishAbs, final double finishOrd)
        {
            final double resultAbs = rScale * (finishAbs - startAbs) + startAbs;
            final double resultOrd = rScale * (finishOrd - startOrd) + startOrd;
            //
            return new double[]{resultAbs, resultOrd};
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            center();
            final double d = d(nextPoint[0], nextPoint[1], realAbsTo, realOrdTo);
            if ( d <= scale(acceptedDistance) )
            {
                System.out.println("\nFinished");
                // Enable canvas panel after finishing the maneuver
                view.setEnabled(true);
                movingTimer.stop();
            }
        }
    }

}