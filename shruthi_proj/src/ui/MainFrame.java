package ui;

import model.User;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

// The main window. CardLayout switches between the four screens.
public class MainFrame extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel cards = new JPanel(cardLayout);
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private ForgotPasswordPanel forgotPanel;
    private DashboardPanel dashboardPanel;

    // Builds the window and shows the login screen.
    public MainFrame() {
        setTitle("ExpenseEase");
        setSize(1050, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        forgotPanel = new ForgotPasswordPanel(this);
        dashboardPanel = new DashboardPanel(this);
        cards.add(loginPanel, "login");
        cards.add(registerPanel, "register");
        cards.add(forgotPanel, "forgot");
        cards.add(dashboardPanel, "dashboard");
        add(cards);
        showLogin();
    }

    // Shows the login screen.
    public void showLogin() {
        loginPanel.resetForm();
        cardLayout.show(cards, "login");
    }

    // Shows the register screen.
    public void showRegister() {
        registerPanel.resetForm();
        cardLayout.show(cards, "register");
    }

    // Shows the forgot password screen.
    public void showForgot() {
        forgotPanel.resetForm();
        cardLayout.show(cards, "forgot");
    }

    // Shows the dashboard for the logged in user.
    public void showDashboard(User user) {
        dashboardPanel.setUser(user);
        dashboardPanel.resetForm();
        cardLayout.show(cards, "dashboard");
        dashboardPanel.startSession();
    }

    // Logs out and returns to the login screen.
    public void logout() {
        showLogin();
    }
}
