package service;

import exception.InvalidInputException;
import model.RecurringTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

// Handles recurring salary, recurring expenses and EMI.
public class RecurringService {

    private ExpenseService expenseService = new ExpenseService();
    private IncomeService incomeService = new IncomeService();

    // Returns all recurring transactions of one user.
    public ArrayList<RecurringTransaction> list(int userId) throws SQLException {
        ArrayList<RecurringTransaction> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT r.recurring_id, r.user_id, r.transaction_type, r.title, "
                    + "r.category_id, c.category_name, r.amount, r.frequency_months, "
                    + "r.start_date, r.next_due_date, r.is_active, r.payments_total, "
                    + "r.payments_done FROM recurring_transactions r "
                    + "LEFT JOIN categories c ON r.category_id = c.category_id "
                    + "WHERE r.user_id = ? ORDER BY r.recurring_id DESC");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new RecurringTransaction(
                        rs.getInt("recurring_id"), rs.getInt("user_id"),
                        rs.getString("transaction_type"), rs.getString("title"),
                        rs.getInt("category_id"), rs.getString("category_name"),
                        rs.getDouble("amount"), rs.getInt("frequency_months"),
                        rs.getString("start_date"), rs.getString("next_due_date"),
                        rs.getInt("is_active") == 1, rs.getInt("payments_total"),
                        rs.getInt("payments_done")));
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return list;
    }

    // Adds a recurring transaction.
    public void add(int userId, String type, String title, int categoryId,
                    double amount, int frequency, String startDate,
                    int paymentsTotal) throws SQLException, InvalidInputException {
        if (title.trim().isEmpty()) {
            throw new InvalidInputException("Title cannot be empty.");
        }
        if (amount <= 0) {
            throw new InvalidInputException("Amount must be greater than zero.");
        }
        if (frequency <= 0) {
            throw new InvalidInputException("Frequency must be greater than zero.");
        }
        if (!isValidDate(startDate)) {
            throw new InvalidInputException("Date must be in yyyy-MM-dd format.");
        }
        if (("EXPENSE".equals(type) || "EMI".equals(type)) && categoryId <= 0) {
            throw new InvalidInputException("Please select a category.");
        }
        if ("EMI".equals(type) && paymentsTotal <= 0) {
            throw new InvalidInputException("Number of payments must be greater than zero.");
        }
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "INSERT INTO recurring_transactions (user_id, transaction_type, "
                    + "title, category_id, amount, frequency_months, start_date, "
                    + "next_due_date, is_active, payments_total, payments_done) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?, 0)");
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, title.trim());
            if (categoryId > 0) {
                ps.setInt(4, categoryId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setDouble(5, amount);
            ps.setInt(6, frequency);
            ps.setString(7, startDate);
            ps.setString(8, startDate);
            ps.setInt(9, "EMI".equals(type) ? paymentsTotal : 0);
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }

    // Updates a recurring transaction.
    public void update(int recurringId, int userId, String title, int categoryId,
                       double amount, int frequency, String nextDueDate)
            throws SQLException, InvalidInputException {
        if (title.trim().isEmpty()) {
            throw new InvalidInputException("Title cannot be empty.");
        }
        if (amount <= 0) {
            throw new InvalidInputException("Amount must be greater than zero.");
        }
        if (frequency <= 0) {
            throw new InvalidInputException("Frequency must be greater than zero.");
        }
        if (!isValidDate(nextDueDate)) {
            throw new InvalidInputException("Date must be in yyyy-MM-dd format.");
        }
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "UPDATE recurring_transactions SET title = ?, category_id = ?, "
                    + "amount = ?, frequency_months = ?, next_due_date = ? "
                    + "WHERE recurring_id = ? AND user_id = ?");
            ps.setString(1, title.trim());
            if (categoryId > 0) {
                ps.setInt(2, categoryId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setDouble(3, amount);
            ps.setInt(4, frequency);
            ps.setString(5, nextDueDate);
            ps.setInt(6, recurringId);
            ps.setInt(7, userId);
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }

    // Pauses or resumes a recurring transaction.
    public void setActive(int recurringId, int userId, boolean active)
            throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "UPDATE recurring_transactions SET is_active = ? "
                    + "WHERE recurring_id = ? AND user_id = ?");
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, recurringId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }

    // Cancels (deletes) a recurring transaction.
    public void cancel(int recurringId, int userId) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "DELETE FROM recurring_transactions "
                    + "WHERE recurring_id = ? AND user_id = ?");
            ps.setInt(1, recurringId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }

    // Processes all due recurring transactions. Call on login/dashboard.
    public void processDue(int userId) throws SQLException, InvalidInputException {
        ArrayList<RecurringTransaction> list = list(userId);
        String today = getToday();
        for (RecurringTransaction rec : list) {
            if (!rec.isActive()) {
                continue;
            }
            String nextDue = rec.getNextDueDate();
            while (nextDue.compareTo(today) <= 0) {
                if ("INCOME".equals(rec.getTransactionType())) {
                    incomeService.addExtraIncome(userId, rec.getAmount(), nextDue,
                            "Recurring: " + rec.getTitle(), rec.getTitle());
                } else {
                    expenseService.addExpense(userId, rec.getCategoryId(),
                            rec.getAmount(), nextDue, rec.getTitle());
                    if ("EMI".equals(rec.getTransactionType())) {
                        rec.setPaymentsDone(rec.getPaymentsDone() + 1);
                        if (rec.getPaymentsTotal() > 0
                                && rec.getPaymentsDone() >= rec.getPaymentsTotal()) {
                            rec.setActive(false);
                        }
                    }
                }
                nextDue = addMonths(nextDue, rec.getFrequencyMonths());
                rec.setNextDueDate(nextDue);
                // Update database.
                Connection con = null;
                PreparedStatement ps = null;
                try {
                    con = DBConnection.getConnection();
                    if (rec.isActive()) {
                        ps = con.prepareStatement(
                                "UPDATE recurring_transactions "
                                + "SET next_due_date = ?, payments_done = ?, is_active = 1 "
                                + "WHERE recurring_id = ?");
                    } else {
                        ps = con.prepareStatement(
                                "UPDATE recurring_transactions "
                                + "SET next_due_date = ?, payments_done = ?, is_active = 0 "
                                + "WHERE recurring_id = ?");
                    }
                    ps.setString(1, nextDue);
                    ps.setInt(2, rec.getPaymentsDone());
                    ps.setInt(3, rec.getRecurringId());
                    ps.executeUpdate();
                } finally {
                    DBConnection.closeAll(con, ps, null);
                }
                if (!rec.isActive()) {
                    break;
                }
            }
        }
    }

    // Adds months to a yyyy-MM-dd date.
    private String addMonths(String date, int months) {
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7)) - 1;
        int day = Integer.parseInt(date.substring(8, 10));
        Calendar cal = new GregorianCalendar(year, month, day);
        cal.add(Calendar.MONTH, months);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", y, m, d);
    }

    // Checks a date text with simple String methods.
    private boolean isValidDate(String date) {
        if (date == null || date.length() != 10) {
            return false;
        }
        if (date.charAt(4) != '-' || date.charAt(7) != '-') {
            return false;
        }
        try {
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8, 10));
            return year >= 2000 && month >= 1 && month <= 12
                    && day >= 1 && day <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Returns today as yyyy-MM-dd.
    private String getToday() {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        return today.toString();
    }
}