package foundation.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        Returns all relatives who are related to `ego` by the `kinship` term.
        For example, f('John Golt', 'uncle') = list of uncles of John Golt.
     */
    public @NotNull List<Vertex> findRelatives(final @NotNull Vertex ego, final @NotNull String relative)
    {
        final @NotNull List<Vertex> result = new LinkedList<>();
        for (final Vertex v : vertices)
        {
            final @NotNull List<String> vToEgoKinship = KinshipDictionary.instance.shorten( kinship(ego, v) );
            if ( vToEgoKinship.size() != 1 )
            {
                continue;
            }
            // Add `v`, the next person, to the result if he/she matches `relative`
            final @NotNull String vToEgo = vToEgoKinship.get(0);
            switch (relative)
            {
                case "children" :
                case "child" :
                {
                    if ( vToEgo.equals("son") || vToEgo.equals("daughter") )
                    {
                        result.add(v);
                    }
                    break;
                }
                case "parents" :
                case "parent" :
                {
                    if ( vToEgo.equals("mother") || vToEgo.equals("father") )
                    {
                        result.add(v);
                    }
                    break;
                }
                case "spouse" :
                {
                    if ( vToEgo.equals("wife") || vToEgo.equals("husband") )
                    {
                        result.add(v);
                    }
                    break;
                }
                default:
                {
                    if ( vToEgo.equals(relative) )
                    {
                        result.add(v);
                    }
                    break;
                }
            }
        }
        //
        return result;
    }

    public @NotNull List<Vertex> vertices()
    {
        return Collections.unmodifiableList( this.vertices );
    }

    /*
            Performs BFS on this tree and finds the shortest path between two specified vertices: `from` and `to`.
            Returns a stack of basic kinship terms which you need to perform on `from` to get to `to` in this tree.
            Basic kinship terms: 'father', 'mother', 'spouse', 'child#index'
         */
    public @NotNull List<String> kinship(final @NotNull Vertex from, final @NotNull Vertex to)
    {
        final @NotNull Map<Vertex, Vertex> parents = bfs(from);
        @Nullable Vertex prev = to;
        final @NotNull List<String> result = new ArrayList<>();
        while ( parents.get(prev) != null )
        {
            result.add( basicKinTerm(prev, parents.get(prev)) );
            prev = parents.get(prev);
        }
        //
        return result;
    }

    /*
        Finds out how the two closest relatives, `from` and `to` are related.
        Returns one of the six basic kinship relations: either father or mother or husband or wife or son or daughter.
     */
    private @NotNull String basicKinTerm(final @NotNull Vertex from, final @NotNull Vertex to)
    {
        final String result;
        final @Nullable Vertex toFather = to.getFather();
        final @Nullable Vertex toMother = to.getMother();
        final @Nullable Vertex toSpouse = to.getSpouse();
        if ((toSpouse != null) && (toSpouse.equals(from)))
        {
            // from is a spouse of to
            result = ( from.profile().isMale() )? "husband" : "wife";
        }
        else if ((toFather != null) && (toFather.equals(from)))
        {
            // from is a father of to
            result = "father";
        }
        else if ((toMother != null) && (toMother.equals(from)))
        {
            // from is a mother of to
            result = "mother";
        }
        else
        {
            // from is a child of to
            result = ( from.profile().isMale() )? "son" : "daughter";
        }
        //
        return result;
    }

    /*
        Computes the so-called "generation distance" between two relatives.
        The "generation distance" is the difference between the generation of person `from` and the generation of
        person `to`.
     */
    public int generationDistance(final @NotNull Vertex from, final @NotNull Vertex to)
    {
        final @NotNull Map<Vertex, Vertex> parentBFS = bfs(from);
        @Nullable Vertex parent = to;
        int distance = 0;
        while (parentBFS.get(parent) != null)
        {
            final @NotNull String kinTerm = basicKinTerm(parent, parentBFS.get(parent));
            // Adjust the distance according to kinship term
            switch (kinTerm)
            {
                case "father" :
                case "mother" :
                {
                    distance++;
                    break;
                }
                case "daughter" :
                case "son" :
                {
                    distance--;
                    break;
                }
            }
            //
            parent = parentBFS.get(parent);
        }
        //
        return distance;
    }

    private @NotNull Map<Vertex, Vertex> bfs(final @NotNull Vertex from)
    {
        vertices.forEach(Vertex::clear);
        final @NotNull Map<Vertex, Vertex> parents = new HashMap<>();
        final @NotNull Queue<Vertex> greyed = new LinkedList<>();
        from.grey();
        greyed.add(from);
        while ( !greyed.isEmpty() )
        {
            // NOTE: `greyed.poll()` cannot produce null, because at this point `greyed` is guaranteed to have at least one element
            final @NotNull Vertex next = Objects.requireNonNull(greyed.poll());
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
    public @Nullable Vertex getVertex(final String firstName, final String lastName)
    {
        return vertices.stream()
            .filter( v ->
            {
                final String vFirst = v.profile().getFirstName().toLowerCase();
                final String first  = firstName.toLowerCase();
                final String vLast  = v.profile().getLastName().toLowerCase();
                final String last   = lastName.toLowerCase();
                final boolean result;
                if (first.isEmpty())
                {
                    result = vLast.equals(last);
                }
                else if (last.isEmpty())
                {
                    result = vFirst.equals(first);
                }
                else
                {
                    result = vFirst.equals(first) && vLast.equals(last);
                }
                //
                return result;
            })
            .findAny()
            .orElse(null);
    }

    /*
        Returns any vertex which has a profile with specified full name.
        If there is no such vertex, returns null.
        Complexity: O(n)
     */
    public @Nullable Vertex getVertex(final @NotNull String fullName)
    {
        final @Nullable Vertex result;
        final int firstSpaceIndex = fullName.indexOf(' ');
        if ( firstSpaceIndex == -1 )
        {
            result = getVertex(fullName, "");
        }
        else
        {
            final String firstName = fullName.substring(0, firstSpaceIndex);
            final String lastName = fullName.substring(firstSpaceIndex + 1, fullName.length());
            result = getVertex(firstName, lastName);
        }
        //
        return result;
    }

}