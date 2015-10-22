import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by wongherlung on 22/10/15.
 */
public class Database {
    static Connection connection = null;
    static final String DATABASE_NAME = "Quizer_Crawler";

    public static void main(String[] args) {
        connect();
    }

    // Connects to the database for you; Quite easy hor.
    public static void connect() {
        Properties config = getConfiguration();
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(config.getProperty("user"));
        dataSource.setPassword(config.getProperty("password"));
        dataSource.setServerName(config.getProperty("localhost"));

        try {
            connection = dataSource.getConnection();
            System.out.println("Successfully connected to database.");

            // Database does not exist, create it
            if (!checkIfDatabaseExists()) {
                executeSQLUpdate("CREATE DATABASE " + DATABASE_NAME);
            }

            // Use Quizer_Crawler
            executeSQLUpdate("USE " + DATABASE_NAME);

            // Creation of tables
            createStatementsTable();
            createVisitedURLsTable();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkIfDatabaseExists() {
        Statement statement = null;
        boolean databaseFound = false;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES");
            while (resultSet.next()) {
                String database = resultSet.getString(1);
                if (database.equals(DATABASE_NAME)) {
                    databaseFound = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return databaseFound;
    }

    private static void executeSQLUpdate(String SQLCommand) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(SQLCommand);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Gets configuration values for connecting to DB
    // config file is found in resources folder
    // Please do not commit config.properties
    private static Properties getConfiguration() {
        File configFile = new File("resources/config.properties");
        FileReader reader = null;
        try {
            reader = new FileReader(configFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Properties props = new Properties();
        try {
            props.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return props;
    }

    // Table Creation

    private static void createStatementsTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            // Create the statement table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS statements (" +
                    "statements VARCHAR(2048), " +
                    "url VARCHAR(2048))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createVisitedURLsTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            // Create the statement table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS visited_urls (" +
                    "url VARCHAR(2048))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
