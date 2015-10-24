import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Created by wongherlung on 22/10/15.
 */
public class Database {
	private Connection connection = null;
	private final String DATABASE_NAME = "quizer_crawler";
	private final String STATEMENTS_TABLE = "statements";
	private final String VISITED_URLS_TABLE = "visited_urls";

	// Connects to the database for you; Quite easy hor.
	public Database() {
		Properties config = getConfiguration();
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser(config.getProperty("user"));
		dataSource.setPassword(config.getProperty("password"));
		dataSource.setServerName(config.getProperty("serverLocation"));

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

	public void insertStatement(String statement, String url) {
		statement = escapeSingleQuotes(statement);
		executeSQLUpdate("INSERT INTO " + STATEMENTS_TABLE + " VALUES ('"
				+ statement + "', '" + url + "')");
	}

	public void insertVisitedtUrl(String url) {
		executeSQLUpdate("INSERT IGNORE INTO " + VISITED_URLS_TABLE + " VALUES ('"
				+ url + "')");
	}

	public LinkedList<String> getVisitedUrls() {
		LinkedList<String> listOfUrls = new LinkedList<String>();
		try {
			ResultSet resultSet = executeSQLQuery("SELECT * FROM "
					+ VISITED_URLS_TABLE);
			while (resultSet.next()) {
				String url = resultSet.getString(1);
				listOfUrls.add(url);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listOfUrls;
	}

	private boolean checkIfDatabaseExists() {
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

	private void executeSQLUpdate(String SQLCommand) {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(SQLCommand);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ResultSet executeSQLQuery(String SQLCommand) {
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = connection.createStatement();
			result = statement.executeQuery(SQLCommand);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	// Gets configuration values for connecting to DB
	// config file is found in resources folder
	// Please do not commit config.properties
	private Properties getConfiguration() {
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

	private String escapeSingleQuotes(String statement) {
		String newStatement = "";
		for (int i = 0; i < statement.length(); i++) {
			if (statement.charAt(i) == '\'') {
				newStatement += "'";
			}
			newStatement += statement.charAt(i);
		}
		return newStatement;
	}

	// Table Creation

	private void createStatementsTable() {
		Statement statement = null;
		try {
			statement = connection.createStatement();

			// Create the statement table
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
					+ STATEMENTS_TABLE + " (" + "statement VARCHAR(1024) NOT NULL, "
					+ "url VARCHAR(1024) NOT NULL)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createVisitedURLsTable() {
		Statement statement = null;
		try {
			statement = connection.createStatement();

			// Create the statement table
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
					+ VISITED_URLS_TABLE + " (" + "url VARCHAR(255) NOT NULL," +
					"PRIMARY KEY (url))");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
