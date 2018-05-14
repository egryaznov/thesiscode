package foundation.main;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class Database
{
    public static final String MALE = "MALE";
    public static final String FEMALE = "FEMALE";
    static final String NO_PHOTO = "EMPTY";
    static final String PHOTO_X_COLUMN = "x";
    static final String PHOTO_Y_COLUMN = "y";
    static final String PHOTO_PATH_COLUMN = "path";
    static final String PERSON_ID_COLUMN = "id";
    static final String FIRST_NAME_COLUMN = "first_name";
    static final String LAST_NAME_COLUMN = "last_name";
    static final String SEX_COLUMN = "sex";
    static final String DATE_OF_BIRTH_COLUMN = "date_of_birth";
    static final String OCCUPATION_COLUMN = "occupation";
    static final String PHONE_NUMBER_COLUMN = "phone_number";
    static final String EMAIL_COLUMN = "email";
    public static final @NotNull Database instance = new Database();
    private Connection connection;

    private Database() {}



    @Nullable Connection getConnection()
    {
        return connection;
    }


    void createNewDatabase(final @NotNull String fullPathToGenealogyDir)
    {
        establishConnection(fullPathToGenealogyDir + "/genealogy.kindb");
        // Create DB schema
        try ( final @NotNull Statement stmt = connection.createStatement() )
        {
            final String createPerson = "CREATE TABLE person ( id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, first_name TEXT NOT NULL, last_name TEXT NOT NULL, sex TEXT NOT NULL, date_of_birth TEXT NOT NULL, occupation TEXT, phone_number TEXT, email TEXT);";
            stmt.execute( createPerson );
            final String createPhoto = "CREATE TABLE photo ( id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, path TEXT NOT NULL, x INTEGER NOT NULL, y INTEGER NOT NULL);";
            stmt.execute( createPhoto );
            final String createHas = "CREATE TABLE has ( person_id INTEGER NOT NULL UNIQUE, photo_id INTEGER NOT NULL UNIQUE, PRIMARY KEY ( person_id ), FOREIGN KEY ( person_id ) REFERENCES person (id), FOREIGN KEY ( photo_id ) REFERENCES photo (id));";
            stmt.execute( createHas );
            final String createBeget = "CREATE TABLE beget ( parent_id INTEGER NOT NULL, child_id INTEGER NOT NULL, FOREIGN KEY ( parent_id ) REFERENCES person (id), FOREIGN KEY ( child_id ) REFERENCES person (id), PRIMARY KEY ( parent_id, child_id ), CHECK (parent_id != child_id));";
            stmt.execute( createBeget );
            final String createWed = "CREATE TABLE wed ( first_spouse_id INTEGER NOT NULL UNIQUE, second_spouse_id INTEGER NOT NULL UNIQUE, date_of_wedding TEXT NOT NULL, FOREIGN KEY ( second_spouse_id ) REFERENCES person (id), PRIMARY KEY ( first_spouse_id ), FOREIGN KEY ( first_spouse_id ) REFERENCES person (id), CHECK (first_spouse_id != second_spouse_id));";
            stmt.execute( createWed );
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /*
        Creates new person in the database and returns his ID.
     */
    int insertPerson(final @NotNull Map<String, String> column)
    {
        int newPersonID;
        final String insert = "INSERT INTO person(first_name, last_name, sex, date_of_birth, occupation, " +
                "phone_number, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (final @NotNull PreparedStatement pstmt = connection.prepareStatement(insert))
        {
            pstmt.setString(1, column.get(FIRST_NAME_COLUMN));
            pstmt.setString(2, column.get(LAST_NAME_COLUMN));
            pstmt.setString(3, column.get(SEX_COLUMN));
            pstmt.setString(4, column.get(DATE_OF_BIRTH_COLUMN));
            pstmt.setString(5, column.get(OCCUPATION_COLUMN));
            pstmt.setString(6, column.get(PHONE_NUMBER_COLUMN));
            pstmt.setString(7, column.get(EMAIL_COLUMN));
            pstmt.executeUpdate();
            newPersonID = getLastInsertedID();
            System.out.printf("%s %s added to the DB at ID %d\n", column.get(FIRST_NAME_COLUMN), column.get(LAST_NAME_COLUMN), newPersonID);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            newPersonID = -1;
        }
        //
        return newPersonID;
    }

    /*
        Deletes all quotes from the string `str` and returns its clean version.
     */
    String wash(final String str)
    {
        final StringBuilder clean = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
        {
            final char next = str.charAt(i);
            if ( (next != '"') && (next != '\'') && (next != '`') )
            {
                clean.append(next);
            }
        }
        return clean.toString();
    }

    /*
        Wraps a string in single quotes.
     */
    String wrap(final String str)
    {
        return "'" + str + "'";
    }

    /*
        Prepares a string to be stored in the DB by deleting all quotes from it first and then wrapping it in
        single quotes.
     */
    String prepareStringValue(final String str)
    {
        return wrap( wash(str) );
    }

    /*
        Inserts a new row in the table `photo` with specified `path`, `x` and `y`.
     */
    int insertPhoto(final @NotNull String path, final double x, final double y)
    {
        int newPhotoID;
        final String insert = "INSERT INTO photo(path, x, y) VALUES (?, ?, ?)";
        try (final @NotNull PreparedStatement pstmt = connection.prepareStatement(insert))
        {
            pstmt.setString(1, path);
            pstmt.setInt(2, Camera.toInt(x));
            pstmt.setInt(3, Camera.toInt(y));
            pstmt.executeUpdate();
            newPhotoID = getLastInsertedID();
        } catch (SQLException e)
        {
            e.printStackTrace();
            newPhotoID = -1;
        }
        //
        return newPhotoID;
    }

    /*
        Removes the record from the table `beget` with specified `parentID` and `childID`.
     */
    void removeParentship(final int parentID, final int childID)
    {
        final String command = String.format("DELETE FROM beget WHERE parent_id = %d AND child_id = %d", parentID, childID);
        issueSQL(command);
    }

    /*
        Removes the record from the table `wed` with specified `firstSpouseID` and `secondSpouseID`.
     */
    void divorce(final int firstSpouseID, final int secondSpouseID)
    {
        final String command = String.format("DELETE FROM wed WHERE" +
                "(first_spouse_id = %d AND second_spouse_id = %d) OR (first_spouse_id = %d AND second_spouse_id = %d)",
                firstSpouseID,
                secondSpouseID,
                secondSpouseID,
                firstSpouseID
        );
        issueSQL(command);
    }

    /*
        Returns the ROWID of the last inserted row.
     */
    private int getLastInsertedID()
    {
        int lastID;
        try ( final @NotNull Statement st = connection.createStatement();
            final @Nullable ResultSet IDResultSet = st.executeQuery("SELECT last_insert_rowid()") )
        {
            if (IDResultSet == null)
            {
                lastID = -1;
            }
            else
            {
                IDResultSet.next();
                lastID = IDResultSet.getInt(1);
            }
        }
        catch (SQLException e)
        {
            lastID = -1;
            e.printStackTrace();
        }
        //
        return lastID;
    }

    /*
        Connects to the specified `databaseFile` and enables the support of foreign keys.
     */
    public void establishConnection(final String databaseFile)
    {
        try
        {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile, config.toProperties());
            System.out.println("Connection to the database has been established");
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /*
        Checks whether a connection has been closed.
     */
    @SuppressWarnings("unused")
    public boolean isClosed()
    {
        try
        {
            return connection.isClosed();
        } catch (SQLException e)
        {
            e.printStackTrace();
            return true;
        }
    }

    /*
        Closes previously established connection to a database and releases the memory.
        NOTE: Use with caution, because connection, once closed, has to be re-established.
     */
    public void closeConnection()
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /*
        Executes UPDATE, INSERT or DELETE query, which doesn't have any result.
     */
    void issueSQL(final String sql)
    {
        try ( final @NotNull Statement s = connection.createStatement() )
        {
            s.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /*
        Returns the ID of the photo of a person with ID == `personID`
     */
    int personIDtoPhotoID(final int personID)
    {
        int photoID = -1;
        final String queryPhotoIDbyPersonID = "SELECT photo_id FROM has WHERE person_id = " + personID;
        try (final @NotNull Statement st = connection.createStatement();
            final @Nullable ResultSet photoIDEntry = st.executeQuery(queryPhotoIDbyPersonID))
        {
            if ( photoIDEntry != null )
            {
                photoID = photoIDEntry.getInt("photo_id");
            }
            else
            {
                System.out.println("There is no person with such ID (" + personID + ") in the DB");
            }
        }
        catch (SQLException e)
        {
            System.out.println("Fatal Error! Cannot find a person with ID " + personID + " in table `has`");
            e.printStackTrace();
            System.exit(personID);
        }
        return photoID;
    }
}