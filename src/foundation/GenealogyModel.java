package foundation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class GenealogyModel
{
    public static final int FEMALE = 0;
    public static final int MALE = 1;
    private @NotNull Connection connection;

    GenealogyModel()
    {
        connectToDatabase();
    }

    private void connectToDatabase()
    {
        final String databaseURL = "jdbc:sqlite:res/ontologies/family1/genealogy.db";
        try
        {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            connection = DriverManager.getConnection(databaseURL, config.toProperties());
            System.out.println("DB connected successfully");
        } catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public @Nullable ResultSet executeQuery(final String query)
    {
        @Nullable ResultSet resultSet;
        try
        {
            final Statement stmt = connection.createStatement();
            resultSet = stmt.executeQuery(query);
        } catch (SQLException e)
        {
            resultSet = null;
            e.printStackTrace();
        }
        return resultSet;
    }
}