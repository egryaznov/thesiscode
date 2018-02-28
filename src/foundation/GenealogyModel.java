package foundation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class GenealogyModel
{
    private final @NotNull ArrayList<Node> nodes = new ArrayList<>();
    private final @NotNull ArrayList<Bond> bonds = new ArrayList<>();

    GenealogyModel()
    {
        prepareNodes();
        prepareBonds();
    }



    /*
        Loads and creates all bonds from the database and stores them in the `bonds` list.
     */
    private void prepareBonds()
    {
        final String queryWed = "SELECT first_spouse_id, second_spouse_id, date_of_wedding FROM wed";
        final String queryBeget = "SELECT * FROM beget";
        try (final @Nullable ResultSet wedTable = Database.instance.executeQuery(queryWed);
             final @Nullable ResultSet begetTable = Database.instance.executeQuery(queryBeget))
        {
            if ((wedTable == null) || (begetTable == null))
            {
                System.out.println("Cannot properly load bonds (wed or beget) from the DB");
                return;
            }
            while (wedTable.next())
            {
                final String weddingDate = wedTable.getString(3);
                @Nullable Node firstNode = findNodeByID( wedTable.getInt(1) );
                @Nullable Node secondNode = findNodeByID( wedTable.getInt(2) );
                // Add marital bond
                if ((firstNode != null) && (secondNode != null))
                {
                    bonds.add( new MaritalBond(firstNode, secondNode, weddingDate) );
                }
            }
            while (begetTable.next())
            {
                @Nullable Node firstNode = findNodeByID( begetTable.getInt(1) );
                @Nullable Node secondNode = findNodeByID( begetTable.getInt(2) );
                // Add parental bond
                if ((firstNode != null) && (secondNode != null))
                {
                    bonds.add( new Bond(firstNode, secondNode) );
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /*
        Loads and creates all nodes from the database and stores them in the `nodes` list.
     */
    private void prepareNodes()
    {
        final String queryAllNodes = "SELECT person.id , person.first_name, person.last_name, person.sex, person.date_of_birth, person.occupation, person.phone_number, person.email, photo.path, photo.x, photo.y " +
                "FROM person, has, photo " +
                "WHERE person.id = has.person_id AND has.photo_id = photo.id";
        try ( final @Nullable ResultSet nodesTable = Database.instance.executeQuery(queryAllNodes) )
        {
            if (nodesTable == null)
            {
                System.out.println("Cannot properly load nodes");
                return;
            }
            //
            while (nodesTable.next())
            {
                final int person_id = nodesTable.getInt(Database.PERSON_ID_COLUMN);
                final String first_name = nodesTable.getString(Database.FIRST_NAME_COLUMN);
                final String last_name = nodesTable.getString(Database.LAST_NAME_COLUMN);
                final String photo_path = nodesTable.getString(Database.PHOTO_PATH_COLUMN);
                final int x = nodesTable.getInt(Database.PHOTO_X_COLUMN);
                final int y = nodesTable.getInt(Database.PHOTO_Y_COLUMN);
                final String sex = nodesTable.getString(Database.SEX_COLUMN);
                final boolean isMale = sex.equals(Database.MALE);
                final String date_of_birth = nodesTable.getString(Database.DATE_OF_BIRTH_COLUMN);
                final String occupation = nodesTable.getString(Database.OCCUPATION_COLUMN);
                final String phone = nodesTable.getString(Database.PHONE_NUMBER_COLUMN);
                final String email = nodesTable.getString(Database.EMAIL_COLUMN);
                @Nullable Image photo;
                try
                {
                    if (photo_path.equals(Database.NO_PHOTO))
                    {
                        photo = null;
                    }
                    else
                    {
                        photo = ImageIO.read(new File(photo_path));
                    }
                } catch (IOException e)
                {
                    System.out.println("Cannot load photo " + photo_path);
                    photo = null;
                    e.printStackTrace();
                }
                // Creating new node...
                nodes.add(new Node(person_id, first_name, last_name, x, y, photo, isMale, date_of_birth, occupation, phone, email));
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /*
         Updates the row in tables 'person' and 'photo' with information stored in the `node`.
     */
    public void commitToDatabase(final @NotNull Node node)
    {
        // Update table 'photo' with new node position
        final String updatePhoto = String.format("UPDATE photo SET x = %d, y = %d WHERE id = %d",
                Camera.toInt( node.getX() ),
                Camera.toInt( node.getY() ),
                node.getID()
        );
        final @NotNull Database db = Database.instance;
        db.issueStatement(updatePhoto);
        // Update table 'person' with new profile information
        final String updatePerson = String.format("UPDATE person SET first_name = %s, last_name = %s, sex = %s, date_of_birth = %s, occupation = %s, phone_number = %s, email = %s WHERE id = %d",
                db.wrap(node.getFirstName()),
                db.wrap(node.getLastName()),
                db.wrap(node.getSex()),
                db.wrap(node.getDateOfBirth()),
                db.wrap(node.getOccupation()),
                db.wrap(node.getPhone()),
                db.wrap(node.getEmail()),
                node.getID()
        );
        db.issueStatement(updatePerson);
    }

    /*
        Returns the marital bond which is linked to the `spouse` or NULL if there is no such bond.
     */
    public @Nullable MaritalBond getMaritalBond(final @NotNull Node spouse)
    {
        return (MaritalBond) bonds.stream()
                .filter(bond -> ((bond instanceof MaritalBond) && bond.isLinkedTo(spouse)))
                .findAny().orElse(null);
    }

    /*
        Checks whether the two nodes `first` and `second` are linked to each other in some marital bond.
     */
    public boolean areMarried(final @NotNull Node first, final @NotNull Node second)
    {
        final @Nullable MaritalBond marriage = getMaritalBond(first);
        return (marriage != null) && (marriage.isBetween(first, second) || marriage.isBetween(second, first));
    }

    /*
        Checks whether the node `person` is linked to some other node by a marital bond.
     */
    public boolean isNotMarried(final @NotNull Node person)
    {
        return getMaritalBond(person) == null;
    }

    /*
        Checks whether two nodes `first` and `second` are connected via some non-marital bond.
     */
    public boolean isParentOrChild(final @NotNull Node first, final @NotNull Node second)
    {
        return bonds.stream().anyMatch( bond -> ( !(bond instanceof MaritalBond)
                && (bond.isBetween(first, second) || bond.isBetween(second, first)) ) );
    }

    /*
        Creates two marital bonds between `firstSpouse` and `SecondSpouse` with the specified
        `weddingDate` and adds them to the 'bonds' list. Also inserts two rows in the table 'wed'.
     */
    public void marry(final @NotNull Node firstSpouse, final @NotNull Node secondSpouse, final String weddingDate)
    {
        final String preparedWeddingDate = Database.instance.prepareStringValue(weddingDate);
        final String insert = String.format(
                "INSERT INTO wed(first_spouse_id, second_spouse_id, date_of_wedding) " +
                        "VALUES (%d, %d, %s), (%d, %d, %s);",
                firstSpouse.getID(),
                secondSpouse.getID(),
                preparedWeddingDate,
                secondSpouse.getID(),
                firstSpouse.getID(),
                preparedWeddingDate
        );
        Database.instance.issueStatement(insert);
        final @NotNull MaritalBond bond1 = new MaritalBond(firstSpouse, secondSpouse, weddingDate);
        final @NotNull MaritalBond bond2 = new MaritalBond(secondSpouse, firstSpouse, weddingDate);
        bonds.add(bond1);
        bonds.add(bond2);
    }

    /*
        Removes two marital bonds between `firstSpouse` and `secondSpouse` from the list 'bonds`.
        Also removes two rows with the corresponding IDs from the table `wed`.
     */
    public void divorce(final @NotNull Node firstSpouse, final @NotNull Node secondSpouse)
    {
        bonds.removeIf( bond -> bond.isBetween(firstSpouse, secondSpouse) || bond.isBetween(secondSpouse, firstSpouse) );
        Database.instance.divorce(firstSpouse.getID(), secondSpouse.getID());
    }

    /*
        Removes all bonds which are linked to the `node` from the list 'bonds'.
     */
    private void deleteAllBondsWithNode(final @NotNull Node node)
    {
        for (final Iterator<Bond> it = bonds.iterator(); it.hasNext();)
        {
            final @NotNull Bond nextBond = it.next();
            if ( nextBond.isLinkedTo(node) )
            {
                // Remove bond from the DB
                final int firstNodeID = nextBond.getHead().getID();
                final int secondNodeID = nextBond.getTail().getID();
                if ( nextBond instanceof MaritalBond )
                {
                    Database.instance.divorce( firstNodeID, secondNodeID );
                }
                else
                {
                    Database.instance.removeParentship( firstNodeID, secondNodeID );
                }
                // Remove the bond from the collection `bonds`
                it.remove();
            }
        }
    }

    /*
        Returns the node which has its ID == `nodeID` or NULL if there is no such node.
     */
    private @Nullable Node findNodeByID(final int nodeID)
    {
        return nodes.stream()
                .filter( node -> node.getID() == nodeID )
                .findAny()
                .orElse(null);
    }

    /*
        Removes parental bond between `first` and `second` from the model.
        Also removes parental row from the table `beget`.
     */
    public void removeParentship(final @NotNull Node first, final @NotNull Node second)
    {
        final @Nullable Bond parentship = bonds.stream()
                .filter( bond -> (!(bond instanceof MaritalBond)
                            && (bond.isBetween(first, second) || bond.isBetween(second, first))) )
                .findAny()
                .orElse(null);
        if (parentship != null)
        {
            final int parentID = parentship.getHead().getID();
            final int childID = parentship.getTail().getID();
            Database.instance.removeParentship(parentID, childID);
            bonds.remove(parentship);
        }
        else
        {
            System.out.println("Cannot find a parental bond to remove: model.removeParentship");
        }
    }

    /*
        Removes:
         1. Specified `node` from the list of all nodes.
         2. All bonds linked to `node` from the list of all bonds.
         3. A personal record from the table `person`.
         4. A record from the table `photo`.
         5. A record from the table `has`.
     */
    public void removeNode(final @NotNull Node node)
    {
        nodes.remove(node);
        deleteAllBondsWithNode(node);
        final int personID = node.getID();
        final int photoID = Database.instance.personIDtoPhotoID(personID);
        final String command = "DELETE FROM has WHERE person_id = " + personID;
        Database.instance.issueStatement(command);
        final String deletePhoto = "DELETE FROM photo WHERE id = " + photoID;
        Database.instance.issueStatement(deletePhoto);
        final String deletePerson = "DELETE FROM person WHERE id = " + personID;
        Database.instance.issueStatement(deletePerson);
    }

    public @NotNull ArrayList<Node> getNodes()
    {
        return nodes;
    }

    public @NotNull ArrayList<Bond> getBonds()
    {
        return bonds;
    }
}
