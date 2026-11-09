import ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Starts the ExpenseEase application.
public class Main {

    // Sets a clean look and opens the main window.
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not set the look and feel.");
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
