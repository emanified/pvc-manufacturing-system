package pvc.panels;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/pvc_project_full.db?foreign_keys=on";
    private static final String USER = "username";
    private static final String PASS = "password";
    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to SQLite database successfully.");
            return conn;
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        connect(); // Test connection
    }
}
