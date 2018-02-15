package foundation;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Image;

public class Node
{
    private boolean selected;
    private @NotNull Color borderColor;
    private final @NotNull Image photo;
    private int x;
    private int y;
    private final int id; // IMPORTANT! This id == id in the DB of the person whom an instance of this class represents

    Node(final int id, final int x, final int y, final @NotNull Image photo, final @NotNull Color borderColor)
    {
        this.id = id;
        this.x = x;
        this.y = y;
        this.photo = photo;
        this.borderColor = borderColor;
    }

    public int getId()
    {
        return id;
    }

    public @NotNull Color getBorderColor()
    {
        return borderColor;
    }

    public @NotNull Image getPhoto()
    {
        return photo;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
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

    public void toggle()
    {
        selected = !selected;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Node && ((Node) obj).id == this.id;
    }
}
