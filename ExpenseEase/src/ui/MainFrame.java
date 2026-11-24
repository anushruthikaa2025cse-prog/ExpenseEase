package ui;

import model.User;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

// The main window. CardLayout switches between the screens.
public class MainFrame extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel cards = new JPanel(cardLayout);
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private ForgotPasswordPanel forgotPanel;
    private DashboardPanel dashboardPanel;
    private StatisticsPanel statisticsPanel;
    private ReportPanel reportPanel;
    private DeletedPanel deletedPanel;
    private SettingsPanel settingsPanel;

    // Builds the window and shows the login screen.
    public MainFrame() {
        setTitle("ExpenseEase");
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        forgotPanel = new ForgotPasswordPanel(this);
        dashboardPanel = new DashboardPanel(this);
        statisticsPanel = new StatisticsPanel(this);
        reportPanel = new ReportPanel(this);
        deletedPanel = new DeletedPanel(this);
        settingsPanel = new SettingsPanel(this);
        cards.add(loginPanel, "login");
        cards.add(registerPanel, "register");
        cards.add(forgotPanel, "forgot");
        cards.add(dashboardPanel, "dashboard");
        cards.add(statisticsPanel, "statistics");
        cards.add(reportPanel, "report");
        cards.add(deletedPanel, "deleted");
        cards.add(settingsPanel, "settings");
        add(cards);
        showLogin();
    }

    public void showLogin() {
        loginPanel.resetForm();
        cardLayout.show(cards, "login");
    }

    public void showRegister() {
        registerPanel.resetForm();
        cardLayout.show(cards, "register");
    }

    public void showForgot() {
        forgotPanel.resetForm();
        cardLayout.show(cards, "forgot");
    }

    public void showDashboard(User user) {
        dashboardPanel.setUser(user);
        dashboardPanel.resetForm();
        cardLayout.show(cards, "dashboard");
        dashboardPanel.startSession();
    }

    public void showStatistics(User user) {
        statisticsPanel.setUser(user);
        statisticsPanel.resetForm();
        cardLayout.show(cards, "statistics");
    }

    public void showReport(User user) {
        reportPanel.setUser(user);
        reportPanel.resetForm();
        cardLayout.show(cards, "report");
    }

    public void showDeleted(User user) {
        deletedPanel.setUser(user);
        deletedPanel.resetForm();
        cardLayout.show(cards, "deleted");
    }

    public void showSettings(User user) {
        settingsPanel.setUser(user);
        settingsPanel.resetForm();
        cardLayout.show(cards, "settings");
    }

    public void logout() {
        showLogin();
    }
}