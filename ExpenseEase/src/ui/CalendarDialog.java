package ui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

// A simple calendar date picker.
public class CalendarDialog extends JDialog implements ActionListener {

    private String selectedDate = null;
    private int year;
    private int month;
    private JLabel monthLabel;
    private JPanel dayPanel;
    private JButton[] dayButtons = new JButton[42];

    // Shows the dialog and returns the selected date as yyyy-MM-dd.
    public static String showDialog(java.awt.Component parent) {
        CalendarDialog dialog = new CalendarDialog(parent);
        dialog.setVisible(true);
        return dialog.selectedDate;
    }

    private CalendarDialog(java.awt.Component parent) {
        super();
        setModal(true);
        Calendar now = Calendar.getInstance();
        year = now.get(Calendar.YEAR);
        month = now.get(Calendar.MONTH);
        setTitle("Select Date");
        setSize(320, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        prev.addActionListener(e -> {
            month--;
            if (month < 0) {
                month = 11;
                year--;
            }
            refreshDays();
        });
        next.addActionListener(e -> {
            month++;
            if (month > 11) {
                month = 0;
                year++;
            }
            refreshDays();
        });
        monthLabel = new JLabel("", JLabel.CENTER);
        top.add(prev, BorderLayout.WEST);
        top.add(monthLabel, BorderLayout.CENTER);
        top.add(next, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        dayPanel = new JPanel(new GridLayout(7, 7));
        String[] names = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String name : names) {
            JLabel label = new JLabel(name, JLabel.CENTER);
            label.setForeground(AppColors.TEXT);
            dayPanel.add(label);
        }
        for (int i = 0; i < 42; i++) {
            dayButtons[i] = new JButton("");
            dayButtons[i].addActionListener(this);
            dayPanel.add(dayButtons[i]);
        }
        add(dayPanel, BorderLayout.CENTER);
        refreshDays();
    }

    // Rebuilds the day buttons for the current month.
    private void refreshDays() {
        String[] names = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthLabel.setText(names[month] + " " + year);
        Calendar cal = new GregorianCalendar(year, month, 1);
        int firstDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int index = 0;
        for (int i = 0; i < 42; i++) {
            dayButtons[i].setText("");
            dayButtons[i].setEnabled(false);
            dayButtons[i].setBackground(AppColors.CARD);
        }
        for (int day = 1; day <= daysInMonth; day++) {
            int pos = firstDay + day - 1;
            dayButtons[pos].setText(String.valueOf(day));
            dayButtons[pos].setEnabled(true);
        }
    }

    // OOP: Overriding actionPerformed from ActionListener.
    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        if (source.getText().isEmpty()) {
            return;
        }
        int day = Integer.parseInt(source.getText());
        selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day);
        dispose();
    }
}