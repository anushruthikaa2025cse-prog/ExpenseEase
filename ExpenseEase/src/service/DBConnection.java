package service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

// Connects the program to MySQL. Settings can be changed in db.properties,
// with environment variables, or with -D system properties.
public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";
    private static final String CONFIG_FILE = "db.properties";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        // Read db.properties from the folder where the program is started.
        Properties fileSettings = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.isFile()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                fileSettings.load(in);
            } catch (IOException e) {
                System.out.println("Could not read " + CONFIG_FILE + ": " + e.getMessage());
            }
        }

        // Priority: -D system property, then environment variable, then file, then default.
        URL = setting("expenseease.db.url", "DB_URL", fileSettings.getProperty("db.url"), DEFAULT_URL);
        USER = setting("expenseease.db.user", "DB_USER", fileSettings.getProperty("db.user"), DEFAULT_USER);
        PASSWORD = setting("expenseease.db.password", "DB_PASSWORD", fileSettings.getProperty("db.password"), DEFAULT_PASSWORD);

        // Loads the MySQL JDBC driver one time.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver not found. Add lib/mysql-connector-j-8.4.0.jar to the classpath.");
        }
    }

    private DBConnection() {
    }

    private static String setting(String systemProperty, String environmentVariable, String fileValue, String fallback) {
        String value = System.getProperty(systemProperty);
        if (isBlank(value)) {
            value = System.getenv(environmentVariable);
        }
        if (isBlank(value)) {
            value = fileValue;
        }
        return isBlank(value) ? fallback : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Opens and returns a database connection.
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new SQLException(
                    "Cannot connect to MySQL as user '" + USER + "' at " + URL + ".\n"
                            + "Check that MySQL is running and that the password is correct.\n"
                            + "Edit db.properties, or set the DB_PASSWORD environment variable.\n"
                            + "MySQL says: " + e.getMessage(),
                    e);
        }
    }

    // Closes the ResultSet, statement and connection safely.
    public static void closeAll(Connection con, PreparedStatement ps, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println("Could not close ResultSet.");
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                System.out.println("Could not close statement.");
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("Could not close connection.");
            }
        }
    }

    // Run this class directly to test the database connection:
    // java -cp "out;lib\mysql-connector-j-8.4.0.jar" service.DBConnection
    public static void main(String[] args) {
        System.out.println("Connecting as user '" + USER + "' to " + URL);
        try (Connection con = getConnection()) {
            System.out.println("Connected to " + con.getMetaData().getDatabaseProductName()
                    + " " + con.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
