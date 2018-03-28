package foundation.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class Vertex
{
    private final int id;
    private final @NotNull Person person;
    private @Nullable Vertex mother;
    private @Nullable Vertex father;
    private final @NotNull ArrayList<Vertex> children = new ArrayList<>();
    private @Nullable Calendar weddingDate;
    private @Nullable Vertex spouse;
    private boolean black = false;
    private boolean grey = false;


    public Vertex(final @NotNull Person person)
    {
        this.person = person;
        this.id = person.getID();
    }



    public void setMother(final @NotNull Vertex mother)
    {
        this.mother = mother;
    }

    public void setFather(final @NotNull Vertex father)
    {
        this.father = father;
    }

    public void setMarriage(final @NotNull Vertex spouse, final @NotNull Calendar weddingDate)
    {
        this.spouse = spouse;
        this.weddingDate = weddingDate;
    }

    public @NotNull Person profile()
    {
        return person;
    }

    public @Nullable Vertex getMother()
    {
        return mother;
    }

    public @Nullable Vertex getFather()
    {
        return father;
    }

    public @Nullable Vertex getSpouse()
    {
        return spouse;
    }

    public @Nullable Calendar getWeddingDate()
    {
        return weddingDate;
    }

    public void addChild( final @NotNull Vertex child )
    {
        children.add(child);
    }

    public int getID()
    {
        return id;
    }

    public @NotNull List<Vertex> children()
    {
        return Collections.unmodifiableList(children);
    }

    public boolean isBlack()
    {
        return black;
    }

    public boolean isGrey()
    {
        return grey;
    }

    public void black()
    {
        this.black = true;
    }

    public void grey()
    {
        this.grey = true;
    }

    public void clear()
    {
        this.black = false;
        this.grey = false;
    }

    public boolean isWhite()
    {
        return !isBlack() && !isGrey();
    }

    /*
        Compares two vertices by birth dates of their profiles.
     */
    @SuppressWarnings("unused")
    public static int compareAges(final @NotNull Vertex v1, final @NotNull Vertex v2)
    {
        final @NotNull Calendar thisBirthDate = Person.stringToCalendar( v1.profile().getDateOfBirth() );
        final @NotNull Calendar otherBirthDate = Person.stringToCalendar( v2.profile().getDateOfBirth() );
        //
        return thisBirthDate.compareTo(otherBirthDate);
    }

    @Override
    public boolean equals(Object obj)
    {
        return ( (obj instanceof Vertex) && (((Vertex) obj).id == this.id) );
    }

    @Override
    public String toString()
    {
        return profile().toString();
    }

    @Override
    public int hashCode()
    {
        return id;
    }
}