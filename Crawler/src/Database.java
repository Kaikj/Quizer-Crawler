import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Created by wongherlung on 22/10/15.
 */
public class Database {
	private Connection connection = null;
	private final String DATABASE_NAME = "quizer_crawler";
	private final String SENTENCES_TABLE = "sentences";
	private final String VISITED_URLS_TABLE = "visited_urls";
	private final String SEED_URLS_TABLE = "seed_urls";

	private final String[] initialSeed = {
		"http://www.straitstimes.com/",
		"http://http://www.bbc.com/",
		"http://www.cnn.com/",
		"http://www.economist.com/",
		"http://www.huffingtonpost.com/",
		"http://www.channelnewsasia.com/",
		"http://www.theguardian.com/"
	};

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
			createSentencesTable();
			createVisitedURLsTable();
			createSeedURLsTable();
			insertInitialSeed();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertSentence(String sentence, String url) {
		sentence = escapeSingleQuotes(sentence);
		System.out.println(sentence);
		executeSQLUpdate("INSERT INTO " + SENTENCES_TABLE +
				" VALUES (?, ?, null)", sentence , url);
	}

	public void insertVisitedUrl(String url) {
		executeSQLUpdate("INSERT IGNORE INTO " + VISITED_URLS_TABLE +
				" VALUES (?)", url);
	}

	public void insertInitialSeed() {
		for (String s : initialSeed) {
			insertSeedURL(s);
		}
	}

	public void insertSeedURL(String url) {
		executeSQLUpdate("INSERT IGNORE INTO " + SEED_URLS_TABLE +
				" VALUES (?)", url);
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

	public boolean checkIfVisited(String url) {
		try {
			ResultSet resultSet = executeSQLQuery("SELECT * FROM " +
					VISITED_URLS_TABLE + " WHERE url = ?", url);
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean checkIfSentenceExist(String sentence) {
		try {
			ResultSet resultSet = executeSQLQuery("SELECT * FROM " +
					SENTENCES_TABLE + " WHERE sentence = ?", sentence);
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean checkIfSeed(String url) {
		try {
			ResultSet resultSet = executeSQLQuery("SELECT * FROM " +
					SEED_URLS_TABLE + " WHERE url LIKE ?", url + "/");
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public LinkedList<String> getSeedUrls() {
		LinkedList<String> listOfUrls = new LinkedList<String>();
		try {
			ResultSet resultSet = executeSQLQuery("SELECT * FROM "
					+ SEED_URLS_TABLE);
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

	private void executeSQLUpdate(String SQLCommand, String... params) {
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(SQLCommand);
			int i = 0;
			for (String s: params) {
				statement.setString(i, s);
				i++;
			}
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ResultSet executeSQLQuery(String SQLCommand, String... params) {
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = connection.prepareStatement(SQLCommand);
			int i = 1;
			for (String s : params) {
				statement.setString(i, s);
				i++;
			}
			result = statement.executeQuery();
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

	private void createSentencesTable() {
		Statement statement = null;
		try {
			statement = connection.createStatement();

			// Create the statement table
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
					+ SENTENCES_TABLE + " (" + "sentence VARCHAR(1024) NOT NULL, "
					+ "url VARCHAR(1024) NOT NULL, " + "accept BOOLEAN)");

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

	private void createSeedURLsTable() {
		Statement statement = null;
		try {
			statement = connection.createStatement();

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
					+ SEED_URLS_TABLE + " (" + "url VARCHAR(255) NOT NULL, " +
					"PRIMARY KEY (url))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
