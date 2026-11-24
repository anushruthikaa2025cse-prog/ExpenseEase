package service;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

// Connects the program to MySQL. Settings can be changed in db.properties,
// with environment variables, with -D system properties, or through the
// connection dialog that appears when the saved settings do not work.
// This makes the application portable: it runs on any computer regardless of
// the MySQL password configured on that machine.
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static final String[] COMMON_PASSWORDS = {
            "", "admin", "root", "password", "123456", "mysql", "toor", "pass123"
    };

    // Mutable so the user can fix them at runtime (portable across machines).
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    // Prevents the connection dialog from popping up repeatedly.
    private static boolean promptShown = false;
    private static boolean triedCommonPasswords = false;

    static {
        loadInitialSettings();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver not found. Add lib/mysql-connector-j-8.4.0.jar to the classpath.");
            System.out.println("Details: " + e.getMessage());
        }
    }

    private DBConnection() {
    }

    private static void loadInitialSettings() {
        Properties fileSettings = loadSettings();
        URL = setting("expenseease.db.url", "DB_URL", fileSettings.getProperty("db.url"), DEFAULT_URL);
        USER = setting("expenseease.db.user", "DB_USER", fileSettings.getProperty("db.user"), DEFAULT_USER);
        PASSWORD = setting("expenseease.db.password", "DB_PASSWORD", fileSettings.getProperty("db.password"), DEFAULT_PASSWORD);
    }

    private static Properties loadSettings() {
        Properties settings = new Properties();

        File configFile = new File(CONFIG_FILE);
        if (configFile.isFile()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                settings.load(in);
                return settings;
            } catch (IOException e) {
                System.out.println("Could not read " + CONFIG_FILE + ": " + e.getMessage());
            }
        }

        try (InputStream in = DBConnection.class.getResourceAsStream("/" + CONFIG_FILE)) {
            if (in != null) {
                settings.load(in);
                return settings;
            }
        } catch (IOException e) {
            System.out.println("Could not read " + CONFIG_FILE + " from classpath: " + e.getMessage());
        }

        File homeConfig = new File(System.getProperty("user.home"), CONFIG_FILE);
        if (homeConfig.isFile()) {
            try (FileInputStream in = new FileInputStream(homeConfig)) {
                settings.load(in);
                return settings;
            } catch (IOException e) {
                System.out.println("Could not read " + homeConfig + ": " + e.getMessage());
            }
        }

        return settings;
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

    public static Connection getConnection() throws SQLException {
        // 1. Try the configured password first.
        try {
            Connection con = openConnection(PASSWORD);
            promptShown = false;
            return con;
        } catch (SQLException lastError) {
            // A missing database is not a password problem: tell the user to
            // create it instead of popping up the password dialog.
            if (isUnknownDatabase(lastError)) {
                throw new SQLException(
                        "Database does not exist: " + databaseName(URL) + ".\n"
                                + "Create it first by running sql/schema.sql (and sql/sample_data.sql).\n"
                                + "MySQL says: " + lastError.getMessage(),
                        lastError);
            }
            // continue to fallbacks
            // (lastError handled below)
            if (isAccessDenied(lastError) && !triedCommonPasswords) {
                triedCommonPasswords = true;
                for (String candidate : COMMON_PASSWORDS) {
                    if (candidate.equals(PASSWORD)) continue;
                    try {
                        Connection con = openConnection(candidate);
                        PASSWORD = candidate;
                        saveSettings();
                        promptShown = false;
                        return con;
                    } catch (SQLException err) {
                        lastError = err;
                        if (!isAccessDenied(err)) break;
                    }
                }
            }

            // 3. Last resort: ask the user.
            if (!promptShown) {
                promptShown = true;
                if (promptForSettings()) {
                    try {
                        Connection con = openConnection(PASSWORD);
                        saveSettings();
                        promptShown = false;
                        return con;
                    } catch (SQLException err) {
                        lastError = err;
                    }
                }
            }

            throw new SQLException(
                    "Cannot connect to MySQL as user '" + USER + "' at " + URL + ".\n"
                            + "Check that MySQL is running and that the host, port, database, "
                            + "username and password are correct.\n"
                            + "On Windows, start the service with: net start MySQL80\n"
                            + "MySQL says: " + (lastError == null ? "unknown error" : lastError.getMessage()),
                    lastError);
    }

    }

    private static Connection openConnection(String candidatePassword) throws SQLException {
        return DriverManager.getConnection(URL, USER, candidatePassword);
    }

    private static boolean isAccessDenied(SQLException e) {
        if (e == null) return false;
        String msg = e.getMessage();
        if (msg == null) return false;
        String lower = msg.toLowerCase();
        return lower.contains("access denied") || lower.contains("using password");
    }

    private static boolean isUnknownDatabase(SQLException e) {
        if (e == null) return false;
        String msg = e.getMessage();
        if (msg == null) return false;
        String lower = msg.toLowerCase();
        return lower.contains("unknown database");
    }

    private static String databaseName(String url) {
        String[] parsed = parseUrl(url);
        return parsed[2];
    }

    private static boolean promptForSettings() {
        if (GraphicsEnvironment.isHeadless()) {
            return false;
        }

        final boolean[] confirmed = { false };
        Runnable task = () -> {
            String[] current = parseUrl(URL);
            JTextField hostField = new JTextField(current[0]);
            JTextField portField = new JTextField(current[1]);
            JTextField dbField = new JTextField(current[2]);
            JTextField userField = new JTextField(USER);
            JPasswordField passwordField = new JPasswordField(PASSWORD);

            JPanel panel = new JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            panel.add(new JLabel("MySQL host:"));
            panel.add(hostField);
            panel.add(new JLabel("Port:"));
            panel.add(portField);
            panel.add(new JLabel("Database:"));
            panel.add(dbField);
            panel.add(new JLabel("Username:"));
            panel.add(userField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(
                    null, panel, "Database Connection Settings",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String host = hostField.getText().trim();
                String port = portField.getText().trim();
                String db = dbField.getText().trim();
                String newUser = userField.getText().trim();
                String newPassword = new String(passwordField.getPassword());

                if (host.isEmpty()) host = "localhost";
                if (port.isEmpty()) port = "3306";
                if (db.isEmpty()) db = "expense_tracker";
                if (newUser.isEmpty()) newUser = "root";

                URL = buildUrl(host, port, db);
                USER = newUser;
                PASSWORD = newPassword;
                confirmed[0] = true;
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(task);
            } catch (Exception e) {
                System.out.println("Could not show the connection dialog: " + e.getMessage());
                return false;
            }
        }
        return confirmed[0];
    }

    private static String[] parseUrl(String url) {
        String host = "localhost";
        String port = "3306";
        String db = "expense_tracker";
        try {
            String s = url;
            int scheme = s.indexOf("://");
            if (scheme >= 0) s = s.substring(scheme + 3);
            int q = s.indexOf('?');
            if (q >= 0) s = s.substring(0, q);
            int slash = s.indexOf('/');
            String hostPort = (slash >= 0) ? s.substring(0, slash) : s;
            String database = (slash >= 0 && slash + 1 < s.length()) ? s.substring(slash + 1) : db;
            int colon = hostPort.indexOf(':');
            if (colon >= 0) {
                host = hostPort.substring(0, colon);
                port = hostPort.substring(colon + 1);
            } else {
                host = hostPort;
            }
            if (!database.isEmpty()) db = database;
        } catch (Exception ignored) {
        }
        return new String[] { host, port, db };
    }

    private static String buildUrl(String host, String port, String db) {
        return "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static void saveSettings() {
        Properties p = new Properties();
        p.setProperty("db.url", URL);
        p.setProperty("db.user", USER);
        p.setProperty("db.password", PASSWORD);

        try (FileOutputStream out = new FileOutputStream(
                new File(System.getProperty("user.home"), CONFIG_FILE))) {
            p.store(out, "ExpenseEase database settings");
        } catch (IOException e) {
            System.out.println("Could not save " + CONFIG_FILE + " to home directory: " + e.getMessage());
        }

        try (FileOutputStream out = new FileOutputStream(new File(CONFIG_FILE))) {
            p.store(out, "ExpenseEase database settings");
        } catch (IOException ignored) {
        }
    }

    public static void closeAll(Connection con, PreparedStatement ps, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println("Could not close ResultSet: " + e.getMessage());
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                System.out.println("Could not close statement: " + e.getMessage());
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("Could not close connection: " + e.getMessage());
            }
        }
    }

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