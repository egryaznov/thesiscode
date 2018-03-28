package foundation.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class FamilyTree
{
    // NOTE: Unmodifiable list!
    private final @NotNull List<Vertex> vertices;

    public FamilyTree(final @NotNull List<Vertex> vertices)
    {
        // this.vertices.addAll(vertices);
        this.vertices = Collections.unmodifiableList(vertices);
    }

    /*
        Returns the vertex which incapsulates the specified person, or null if the is no such vertex.
        Complexity: O(n)
     */
    public @Nullable Vertex vertex(final @NotNull Person person)
    {
        return vertices.stream()
                .filter(v -> v.getID() == person.getID())
                .findAny()
                .orElse(null);
    }

    public @NotNull List<Vertex> vertices()
    {
        return Collections.unmodifiableList( vertices );
    }

    /*
        Performs BFS on this tree and finds the shortest path between two specified vertices: `from` and `to`.
        Returns a stack of basic kinship terms which you need to perform on `from` to get to `to` in this tree.
        Basic kinship terms: 'father', 'mother', 'spouse', 'child#index'
     */
    public @NotNull String kinship(final @NotNull Vertex from, final @NotNull Vertex to)
    {
        final @NotNull Map<Vertex, Vertex> parents = bfs(from);
        @Nullable Vertex prev = to;
        final StringBuilder kinship = new StringBuilder();
        while ( parents.get(prev) != null )
        {
            kinship.append( basicKinTerm(prev, parents.get(prev)) );
            kinship.append(" of ");
            prev = parents.get(prev);
        }
        kinship.append("me");
        //
        return kinship.toString();
    }

    private String basicKinTerm(final @NotNull Vertex from, final @NotNull Vertex to)
    {
        final @Nullable Vertex toFather = to.getFather();
        final @Nullable Vertex toMother = to.getMother();
        final @Nullable Vertex toSpouse = to.getSpouse();
        if ((toSpouse != null) && (toSpouse.equals(from)))
        {
            return "spouse";
        }
        if ((toFather != null) && (toFather.equals(from)))
        {
            return "father";
        }
        if ((toMother != null) && (toMother.equals(from)))
        {
            return "mother";
        }
        else
        {
            return "child";
        }
    }


    private Map<Vertex, Vertex> bfs(final @NotNull Vertex from)
    {
        vertices.forEach(Vertex::clear);
        final @NotNull Map<Vertex, Vertex> parents = new HashMap<>();
        final @NotNull Queue<Vertex> greyed = new LinkedList<>();
        from.grey();
        greyed.add(from);
        while ( !greyed.isEmpty() )
        {
            final @NotNull Vertex next = greyed.poll();
            visit(next.getFather(), parents, next, greyed);
            visit(next.getMother(), parents, next, greyed);
            visit(next.getSpouse(), parents, next, greyed);
            for (final Vertex child : next.children())
            {
                visit(child, parents, next, greyed);
            }
            next.black();
        }
        //
        return parents;
    }

    private void visit(final @Nullable Vertex child,
                       final @NotNull Map<Vertex, Vertex> parents,
                       final @NotNull Vertex parent,
                       final @NotNull Queue<Vertex> greyed)
    {
        if (child != null)
        {
            if (child.isWhite())
            {
                child.grey();
                parents.put(child, parent);
                greyed.add(child);
            }
        }
    }

    /*
        Returns any vertex which has a profile with specified first and last name.
        If there is no such vertex, returns null.
        Complexity: O(n)
     */
    public @Nullable Vertex findVertex(final String firstName, final String lastName)
    {
        return vertices.stream()
            .filter( v ->
            {
                final String vFirst = v.profile().getFirstName().toLowerCase();
                final String first  = firstName.toLowerCase();
                final String vLast  = v.profile().getLastName().toLowerCase();
                final String last   = lastName.toLowerCase();
                return vFirst.equals(first) && vLast.equals(last);
            })
            .findAny()
            .orElse(null);
    }

}