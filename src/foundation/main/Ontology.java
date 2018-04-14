package foundation.main;

import foundation.entity.Bond;
import foundation.entity.FamilyTree;
import foundation.entity.MaritalBond;
import foundation.entity.Person;
import foundation.entity.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Ontology
{
    private final @NotNull ArrayList<Person> people = new ArrayList<>();
    private final @NotNull ArrayList<Bond> bonds = new ArrayList<>();
    private @Nullable FamilyTree tree;

    public Ontology()
    {
        preparePeople();
        prepareBonds();
    }



    /*
        Returns the family tree graph with an array of vertices as persons.
     */
    private @NotNull FamilyTree generateFamilyTree()
    {
        final @NotNull ArrayList<Vertex> vertices = new ArrayList<>();
        final @NotNull Map<Integer, Vertex> personToVertex = new HashMap<>();
        // Instantiate all vertices first
        for (Person person : people)
        {
            final @NotNull Vertex nextVertex = new Vertex(person);
            personToVertex.put(person.getID(), nextVertex);
            vertices.add(nextVertex);
        }
        // Now connect the vertices by bonds
        for (Bond link : bonds)
        {
            if (link instanceof MaritalBond)
            {
                // Add a marital bond
                final @NotNull MaritalBond marriage = (MaritalBond)link;
                final @NotNull Calendar weddingDate = Person.stringToCalendar( marriage.getDateOfWedding() );
                // Spouse fields are not null because we already added all people into the map personToVertex
                final @NotNull Vertex fromSpouse = personToVertex.get(marriage.getHead().getID());
                final @NotNull Vertex toSpouse = personToVertex.get(marriage.getTail().getID());
                fromSpouse.setMarriage(toSpouse, weddingDate);
                toSpouse.setMarriage(fromSpouse, weddingDate);
            }
            else
            {
                // Add a parental edge
                final @NotNull Vertex child  = personToVertex.get(link.getTail().getID());
                final @NotNull Vertex parent = personToVertex.get(link.getHead().getID());
                parent.addChild(child);
                if ( parent.profile().isMale() )
                {
                    child.setFather(parent);
                }
                else
                {
                    child.setMother(parent);
                }
            }
        }
        //
        return new FamilyTree( vertices );
    }

    /*
        Loads and creates all entity from the database and stores them in the `entity` list.
     */
    private void prepareBonds()
    {
        final String queryWed = "SELECT first_spouse_id, second_spouse_id, date_of_wedding FROM wed";
        final String queryBeget = "SELECT * FROM beget";
        try (final @NotNull var stWed = Objects.requireNonNull(Database.instance.getConnection()).createStatement();
             final @NotNull var stBeget = Database.instance.getConnection().createStatement();
             final @Nullable var wedTable = stWed.executeQuery(queryWed);
             final @Nullable var begetTable = stBeget.executeQuery(queryBeget))
        {
            if ((wedTable == null) || (begetTable == null))
            {
                System.out.println("Cannot properly load entity (wed or beget) from the DB");
                return;
            }
            while (wedTable.next())
            {
                @Nullable Person firstPerson = findPersonByID( wedTable.getInt(1) );
                @Nullable Person secondPerson = findPersonByID( wedTable.getInt(2) );
                final String weddingDate = wedTable.getString(3);
                // Add marital bond
                if ((firstPerson != null) && (secondPerson != null))
                {
                    bonds.add( new MaritalBond(firstPerson, secondPerson, weddingDate) );
                }
            }
            while (begetTable.next())
            {
                @Nullable Person firstPerson = findPersonByID( begetTable.getInt(1) );
                @Nullable Person secondPerson = findPersonByID( begetTable.getInt(2) );
                // Add parental bond
                if ((firstPerson != null) && (secondPerson != null))
                {
                    bonds.add( new Bond(firstPerson, secondPerson) );
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /*
        Loads and creates all people from the database and stores them in the `people` list.
     */
    private void preparePeople()
    {
        final String queryAllPeople = "SELECT person.id , person.first_name, person.last_name, person.sex, person.date_of_birth, person.occupation, person.phone_number, person.email, photo.path, photo.x, photo.y " +
                "FROM person, has, photo " +
                "WHERE person.id = has.person_id AND has.photo_id = photo.id";
        try (final @NotNull Statement st = Objects.requireNonNull(Database.instance.getConnection()).createStatement();
             final @Nullable ResultSet personsTable = st.executeQuery(queryAllPeople) )
        {
            if (personsTable == null)
            {
                System.out.println("Cannot properly load people");
                return;
            }
            //
            while (personsTable.next())
            {
                final int person_id = personsTable.getInt(Database.PERSON_ID_COLUMN);
                final String first_name = personsTable.getString(Database.FIRST_NAME_COLUMN);
                final String last_name = personsTable.getString(Database.LAST_NAME_COLUMN);
                final String photo_path = personsTable.getString(Database.PHOTO_PATH_COLUMN);
                final int x = personsTable.getInt(Database.PHOTO_X_COLUMN);
                final int y = personsTable.getInt(Database.PHOTO_Y_COLUMN);
                final String sex = personsTable.getString(Database.SEX_COLUMN);
                final boolean isMale = sex.equals(Database.MALE);
                final String date_of_birth = personsTable.getString(Database.DATE_OF_BIRTH_COLUMN);
                final String occupation = personsTable.getString(Database.OCCUPATION_COLUMN);
                final String phone = personsTable.getString(Database.PHONE_NUMBER_COLUMN);
                final String email = personsTable.getString(Database.EMAIL_COLUMN);
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
                people.add(new Person(person_id, first_name, last_name, x, y, photo, isMale, date_of_birth, occupation, phone, email));
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /*
         Updates the row in tables 'person' and 'photo' with information stored in the `person`.
     */
    void commitToDatabase(final @NotNull Person person)
    {
        // Update table 'photo' with new person position
        final String updatePhoto = String.format("UPDATE photo SET x = %d, y = %d WHERE id = %d",
                Camera.toInt( person.getX() ),
                Camera.toInt( person.getY() ),
                person.getID()
        );
        final @NotNull Database db = Database.instance;
        db.issueSQL(updatePhoto);
        // Update table 'person' with new profile information
        final String updatePerson = String.format("UPDATE person SET first_name = %s, last_name = %s, sex = %s, date_of_birth = %s, occupation = %s, phone_number = %s, email = %s WHERE id = %d",
                db.wrap(person.getFirstName()),
                db.wrap(person.getLastName()),
                db.wrap(person.getSex()),
                db.wrap(person.getDateOfBirth()),
                db.wrap(person.getOccupation()),
                db.wrap(person.getPhone()),
                db.wrap(person.getEmail()),
                person.getID()
        );
        db.issueSQL(updatePerson);
    }

    /*
        Returns the marital bond which is linked to the `spouse` or NULL if there is no such bond.
     */
    @Nullable MaritalBond getMaritalBond(final @NotNull Person spouse)
    {
        return (MaritalBond) bonds.stream()
                .filter(bond -> ((bond instanceof MaritalBond) && bond.isLinkedTo(spouse)))
                .findAny().orElse(null);
    }

    /*
        Checks whether the two people `first` and `second` are linked to each other in some marital bond.
     */
    boolean areMarried(final @NotNull Person first, final @NotNull Person second)
    {
        final @Nullable MaritalBond marriage = getMaritalBond(first);
        return (marriage != null) && (marriage.isBetween(first, second) || marriage.isBetween(second, first));
    }

    /*
        Checks whether the node `person` is linked to some other node by a marital bond.
        Returns true iff it is.
     */
    @SuppressWarnings("unused")
    public boolean isMarried(final @NotNull Person person)
    {
        return !isNotMarried(person);
    }

    /*
        Checks whether the node `person` is linked to some other node by a marital bond.
        Returns true iff it isn't.
     */
    boolean isNotMarried(final @NotNull Person person)
    {
        return getMaritalBond(person) == null;
    }

    /*
        Checks whether two people `first` and `second` are connected via some non-marital bond.
     */
    boolean isParentOrChild(final @NotNull Person first, final @NotNull Person second)
    {
        return bonds.stream().anyMatch( bond -> ( !(bond instanceof MaritalBond)
                && (bond.isBetween(first, second) || bond.isBetween(second, first)) ) );
    }

    /*
        Creates two marital entity between `firstSpouse` and `SecondSpouse` with the specified
        `weddingDate` and adds them to the 'entity' list. Also inserts two rows in the table 'wed'.
     */
    void marry(final @NotNull Person firstSpouse, final @NotNull Person secondSpouse, final String weddingDate)
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
        Database.instance.issueSQL(insert);
        final @NotNull MaritalBond bond1 = new MaritalBond(firstSpouse, secondSpouse, weddingDate);
        final @NotNull MaritalBond bond2 = new MaritalBond(secondSpouse, firstSpouse, weddingDate);
        bonds.add(bond1);
        bonds.add(bond2);
        // Drop the pre-generated family tree because it's no longer contains the right information
        tree = null;
    }

    /*
        Removes two marital entity between `firstSpouse` and `secondSpouse` from the list 'entity`.
        Also removes two rows with the corresponding IDs from the table `wed`.
     */
    void divorce(final @NotNull Person firstSpouse, final @NotNull Person secondSpouse)
    {
        bonds.removeIf( bond -> bond.isBetween(firstSpouse, secondSpouse) || bond.isBetween(secondSpouse, firstSpouse) );
        Database.instance.divorce(firstSpouse.getID(), secondSpouse.getID());
        // Drop the pre-generated family tree because it's no longer contains the right information
        tree = null;
    }

    /*
        Removes all entity which are linked to the `person` from the list 'entity'.
     */
    private void deleteAllBondsWithNode(final @NotNull Person person)
    {
        for (final Iterator<Bond> it = bonds.iterator(); it.hasNext();)
        {
            final @NotNull Bond nextBond = it.next();
            if ( nextBond.isLinkedTo(person) )
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
                // Remove the bond from the collection `entity`
                it.remove();
            }
        }
    }

    /*
        Returns the person which has its ID == `nodeID` or NULL if there is no such node.
     */
    private @Nullable Person findPersonByID(final int nodeID)
    {
        return people.stream()
                .filter( node -> node.getID() == nodeID )
                .findAny()
                .orElse(null);
    }

    /*
        Removes parental bond between `first` and `second` from the model.
        Also removes parental row from the table `beget`.
     */
    void removeParentship(final @NotNull Person first, final @NotNull Person second)
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
            // Drop the pre-generated family tree because it's no longer contains the right information
            tree = null;
        }
        else
        {
            System.out.println("Cannot find a parental bond to remove: model.removeParentship");
        }
    }

    /*
        Removes:
         1. Specified `person` from the list of all people.
         2. All entity linked to `person` from the list of all entity.
         3. A personal record from the table `person`.
         4. A record from the table `photo`.
         5. A record from the table `has`.
     */
    void removePerson(final @NotNull Person person)
    {
        people.remove(person);
        deleteAllBondsWithNode(person);
        final int personID = person.getID();
        final int photoID = Database.instance.personIDtoPhotoID(personID);
        final String command = "DELETE FROM has WHERE person_id = " + personID;
        Database.instance.issueSQL(command);
        final String deletePhoto = "DELETE FROM photo WHERE id = " + photoID;
        Database.instance.issueSQL(deletePhoto);
        final String deletePerson = "DELETE FROM person WHERE id = " + personID;
        Database.instance.issueSQL(deletePerson);
        // Drop the pre-generated family tree because it's no longer contains the right information
        tree = null;
    }

    void addPerson(final @NotNull Person person)
    {
        people.add(person);
        // Drop the pre-generated family tree because it's no longer contains the right information
        tree = null;
    }

    void addBond(final @NotNull Bond bond)
    {
        bonds.add(bond);
        // Drop the pre-generated family tree because it's no longer contains the right information
        tree = null;
    }

    /*
        Returns the read-only version of the ArrayList `people`.
     */
    @NotNull List<Person> getPeople()
    {
        return Collections.unmodifiableList(people);
    }

    /*
        Returns the read-only version of the ArrayList `bonds`.
     */
    @NotNull List<Bond> getBonds()
    {
        return Collections.unmodifiableList(bonds);
    }

    /**
        Returns a pre-generated family tree, or, if entity was changed after the last generation of the tree,
        generates it again.
        @return the newest version of the family tree from this model.
     */
    public @NotNull FamilyTree tree()
    {
        if (tree == null)
        {
            tree = generateFamilyTree();
        }
        //
        return tree;
    }
}
