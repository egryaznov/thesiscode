package foundation.entity;

import foundation.lisp.types.TDate;
import foundation.main.Camera;
import foundation.main.Database;
import foundation.main.GenealogyView;
import foundation.main.MainFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Person
{
    private static final @NotNull Color FEMALE_FONT_COLOR = new Color(255, 213, 226);
    private static final @NotNull Color MALE_FONT_COLOR = new Color(193, 245, 255);
    private static final @NotNull Color MALE_NODE_COLOR = new Color(0, 162, 232);
    private static final @NotNull Color FEMALE_NODE_COLOR = new Color(255, 102, 179);
    private static final @NotNull Color SELECTED_NODE_COLOR = Color.RED;
    private boolean selected;
    private boolean male;
    private String firstName;
    private String lastName;
    private @Nullable
    final Image photo;
    private @NotNull String dateOfBirth;
    private String occupation;
    private String phone;
    private String email;
    private double x;
    private double y;
    private final int id; // IMPORTANT! This id == id in the DB of the getPerson which an instance of this class represents

    public Person(final int id, final String firstName, final String lastName, final double x, final double y,
           final @Nullable Image photo, final boolean male, final @NotNull String dateOfBirth,
           final String occupation, final String phone, final String email)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.x = x;
        this.y = y;
        this.photo = photo;
        this.male = male;
        this.dateOfBirth = dateOfBirth;
        this.occupation = occupation;
        this.phone = phone;
        this.email = email;
    }



    public int getID()
    {
        return id;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public void select()
    {
        selected = true;
    }

    public void deselect()
    {
        selected = false;
    }

    public void move(final double offsetX, final double offsetY)
    {
        this.x += offsetX;
        this.y += offsetY;
    }

    public boolean isMale()
    {
        return male;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    @NotNull Color getColor()
    {
        return (selected)? Person.SELECTED_NODE_COLOR : (male)? Person.MALE_NODE_COLOR : Person.FEMALE_NODE_COLOR;
    }

    public @NotNull String getDateOfBirth()
    {
        return dateOfBirth;
    }

    /*
        Either "MALE" or "FEMALE"
     */
    public String getSex()
    {
        return ( male )? Database.MALE : Database.FEMALE;
    }

    public static String calendarToString(final @NotNull Calendar cal)
    {
        final int day   = cal.get(Calendar.DAY_OF_MONTH);
        // Since months start with 0 in Calendar, we need to increment the original value
        final int month = cal.get(Calendar.MONTH) + 1;
        final int year  = cal.get(Calendar.YEAR);
        return String.format("%d.%d.%d", day, month, year);
    }

    /*
        Converts the string date of format "dd.mm.year" to a calendar object.
     */
    public static @NotNull Calendar stringToCalendar(final String rawDate)
    {
        if (rawDate.equals(TDate.NOW_KEYWORD))
        {
            return Calendar.getInstance();
        }
        // Load date of birth from DB
        final @NotNull int[] dayMonthYear = Arrays.stream(rawDate.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
        final int day = dayMonthYear[0];
        final int month = dayMonthYear[1] - 1; // Months start with 0, so February has index 1, for example
        final int year = dayMonthYear[2];
        return new GregorianCalendar(year, month, day);
    }

    public void setDateOfBirth(@NotNull final String dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    public String getOccupation()
    {
        return occupation;
    }

    public void setOccupation(String occupation)
    {
        this.occupation = occupation;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setSex(final String sex)
    {
        this.male = sex.equals(Database.MALE);
    }

    public void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }

    /*
        Draws the node on the `view` with the specified `g2d` object.
     */
    public void draw(final @NotNull GenealogyView view, final @NotNull Graphics2D g2d)
    {
        if (photo == null)
        {
            drawNodeWithName(g2d, view.getCamera());
        }
        else
        {
            drawNodeWithPhoto(g2d, view.getCamera());
        }
    }

    private void drawNodeWithPhoto(final @NotNull Graphics2D g2d, final @NotNull Camera camera)
    {
        final double scaledImageDiameter = camera.scale(GenealogyView.DEFAULT_CIRCLE_DIAMETER);
        g2d.setClip(
                new Ellipse2D.Float(camera.toScreenX(x),
                        camera.toScreenY(y),
                        Camera.toInt( scaledImageDiameter ),
                        Camera.toInt( scaledImageDiameter ))
        );
        g2d.drawImage(
                photo,
                camera.toScreenX(x),
                camera.toScreenY(y),
                Camera.toInt( scaledImageDiameter ),
                Camera.toInt( scaledImageDiameter ),
                null
        );
        // Remove the clip from the graphics
        g2d.setClip(null);
        // Draw a circled border around the photo
        g2d.setColor( getColor() );
        g2d.drawOval(
                camera.toScreenX(x),
                camera.toScreenY(y),
                Camera.toInt( scaledImageDiameter ),
                Camera.toInt( scaledImageDiameter )
        );
    }

    private void drawNodeWithName(final @NotNull Graphics2D g2d, final @NotNull Camera camera)
    {
        final double scaledFrameDiameter = camera.scale(GenealogyView.DEFAULT_CIRCLE_DIAMETER);
        // Draw a colored circle
        g2d.setColor( getColor() );
        if ( MainFrame.graphicsOptions.get(MainFrame.FILLED_CIRCLES) )
        {
            g2d.fill( new Ellipse2D.Double(
                    camera.toScreenX(x),
                    camera.toScreenY(y),
                    scaledFrameDiameter,
                    scaledFrameDiameter)
            );
        }
        else
        {
            g2d.drawOval(
                    camera.toScreenX(x),
                    camera.toScreenY(y),
                    Camera.toInt(scaledFrameDiameter),
                    Camera.toInt(scaledFrameDiameter)
            );
        }
        // Prepare environment
        final @NotNull Font gabriola = new Font("Gabriola", Font.PLAIN, Camera.toInt(18));
        g2d.setFont(gabriola);
        final @NotNull FontMetrics metrics = g2d.getFontMetrics();
        // Determine the length of the label
        final String name = firstName + " " + lastName;
        final double scaledMaxNameWidth = camera.scale(GenealogyView.DEFAULT_CIRCLE_DIAMETER - 4.5);
        String label = name;
        for (int length = label.length(); length > 0; length--)
        {
            label = label.substring(0, length);
            if ( metrics.getStringBounds(label, g2d).getWidth() <= scaledMaxNameWidth )
            {
                break;
            }
        }
        // If the whole label fits into the circle, then increase font size
        if (label.length() == name.length())
        {
            do {
                final @NotNull Font font = g2d.getFont();
                g2d.setFont(font.deriveFont(Font.PLAIN, font.getSize() + 1));
            } while( g2d.getFontMetrics().getStringBounds(label, g2d).getWidth() <= scaledMaxNameWidth );
            // Final font size is one point bigger than we need, so decrement it
            final @NotNull Font font = g2d.getFont();
            g2d.setFont(font.deriveFont(Font.PLAIN, font.getSize() - 1));
        }
        // Determine where we should draw the label
        final double labelBaseX = x + 3;
        final double labelBaseY = y + GenealogyView.DEFAULT_CIRCLE_RADIUS + 3.5;
        // Draw a label at determined coordinates
        if ( MainFrame.graphicsOptions.get(MainFrame.FILLED_CIRCLES) )
        {
            g2d.setColor( (male)? Person.MALE_FONT_COLOR : Person.FEMALE_FONT_COLOR );
        }
        else
        {
            g2d.setColor(Color.WHITE);
        }
        g2d.drawString(label, camera.toScreenX(labelBaseX), camera.toScreenY(labelBaseY));
    }

    @Override
    public String toString()
    {
        return String.format("#%d %s %s", id, firstName, lastName);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Person && ((Person) obj).id == this.id;
    }
}