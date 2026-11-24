import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import service.DBConnection;
import ui.MainFrame;

// Starts the ExpenseEase application.
public class Main {
    // Sets a clean look and opens the main window.
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not set the look and feel.");
        }

        // Check the database connection up front. If the saved settings do not
        // work, the user is prompted for the correct ones, so the application
        // can run on any computer regardless of its MySQL password.
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Connected to " + con.getMetaData().getDatabaseProductName()
                    + " " + con.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}