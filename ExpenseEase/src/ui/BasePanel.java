package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

// OOP: Abstract class. Every screen panel extends this class.
public abstract class BasePanel extends JPanel {

    protected static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    protected static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);

    // Sets the common page background.
    public BasePanel() {
        setBackground(AppColors.PAGE);
    }

    // OOP: Abstract method - every screen must override it.
    public abstract void resetForm();

    // Creates a white card panel for the forms.
    protected JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(AppColors.CARD);
        card.setBorder(new EmptyBorder(30, 35, 30, 35));
        return card;
    }

    // Creates a big dark green title.
    protected JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(AppColors.TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Creates a normal dark green label.
    protected JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT);
        label.setForeground(AppColors.TEXT);
        return label;
    }

    // Creates a text field with a light green border.
    protected JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT);
        field.setPreferredSize(new Dimension(280, 34));
        field.setMaximumSize(new Dimension(280, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC8E6C9)),
                new EmptyBorder(4, 8, 4, 8)));
        return field;
    }

    // Creates a password field with a light green border.
    protected JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT);
        field.setPreferredSize(new Dimension(280, 34));
        field.setMaximumSize(new Dimension(280, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC8E6C9)),
                new EmptyBorder(4, 8, 4, 8)));
        return field;
    }

    // Creates a flat coloured button.
    protected JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 18, 9, 18));
        return button;
    }

    // Creates a small link-style button.
    protected JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT);
        button.setForeground(AppColors.TEXT);
        button.setBackground(AppColors.CARD);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Adds a label and a field to a vertical form.
    protected void addField(JPanel panel, String text, JComponent field) {
        JLabel label = createLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(field);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
    }
}
