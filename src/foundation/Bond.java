package foundation;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class Bond
{
    private final @NotNull Node firstNode;
    private final @NotNull Node secondNode;
    private final @NotNull Color color;

    Bond(final @NotNull Node firstNode, final @NotNull Node secondNode, final @NotNull Color color)
    {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.color = color;
    }

    public Node getFirstNode()
    {
        return firstNode;
    }

    public Node getSecondNode()
    {
        return secondNode;
    }

    public Color getColor()
    {
        return color;
    }
}
