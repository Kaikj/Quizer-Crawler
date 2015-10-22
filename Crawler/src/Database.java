import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by wongherlung on 22/10/15.
 */
public class Database {
    static Connection connection = null;

    public static void main(String[] args) {
        connect();
    }

    // Connects to the database for you; Quite easy hor.
    public static void connect() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser("root");
        dataSource.setPassword("password");
        dataSource.setServerName("localhost");

        try {
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES");
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
