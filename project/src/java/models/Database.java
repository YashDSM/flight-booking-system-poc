package models;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
public final class Database {
 
    private static final String URL =
            "jdbc:h2:mem:flightdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String USER = "skyline";
    private static final String PASSWORD = "sky123";
 
    private Database() {
        // Utility class; do not instantiate.
    }
 
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("H2 driver is not on the runtime classpath.", ex);
        }
 
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}