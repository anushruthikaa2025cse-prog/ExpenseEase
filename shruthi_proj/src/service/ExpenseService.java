package service;

import exception.InvalidInputException;
import model.Expense;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

// Handles add, edit, delete and list of expenses.
public class ExpenseService {

    // Adds one expense for a user.
    public void addExpense(int userId, int categoryId, double amount,
                           String date, String description)
            throws SQLException, InvalidInputException {
        checkExpenseInput(amount, date, description);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "INSERT INTO expenses (user_id, category_id, amount, "
                    + "expense_date, description) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setDouble(3, amount);
            ps.setString(4, date);
            ps.setString(5, description.trim());
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }

    // Updates one expense that belongs to the user.
    public void updateExpense(int expenseId, int userId, int categoryId,
                              double amount, String date, String description)
            throws SQLException, InvalidInputException {
        checkExpenseInput(amount, date, description);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "UPDATE expenses SET category_id = ?, amount = ?, "
                    + "expense_date = ?, description = ? "
                    + "WHERE expense_id = ? AND user_id = ?");
            ps.setInt(1, categoryId);
            ps.setDouble(2, amount);
            ps.setString(3, date);
            ps.setString(4, description.trim());
            ps.setInt(5, expenseId);
            ps.setInt(6, userId);
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }

    // Deletes one expense. The trigger writes it to expense_log.
    public void deleteExpense(int userId, int expenseId) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "DELETE FROM expenses WHERE expense_id = ? AND user_id = ?");
            ps.setInt(1, expenseId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }

    // Returns all expenses of the user, newest first.
    public ArrayList<Expense> listExpenses(int userId) throws SQLException {
        ArrayList<Expense> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT expense_id, user_id, category_id, category_name, "
                    + "amount, expense_date, description "
                    + "FROM expense_details_view WHERE user_id = ? "
                    + "ORDER BY expense_date DESC, expense_id DESC");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Expense(rs.getInt("expense_id"), rs.getInt("user_id"),
                        rs.getInt("category_id"), rs.getString("category_name"),
                        rs.getDouble("amount"), rs.getString("expense_date"),
                        rs.getString("description")));
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return list;
    }

    // Finds one expense of the user, or returns null.
    public Expense findExpense(int userId, int expenseId) throws SQLException {
        Expense expense = null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT expense_id, user_id, category_id, category_name, "
                    + "amount, expense_date, description "
                    + "FROM expense_details_view "
                    + "WHERE expense_id = ? AND user_id = ?");
            ps.setInt(1, expenseId);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                expense = new Expense(rs.getInt("expense_id"), rs.getInt("user_id"),
                        rs.getInt("category_id"), rs.getString("category_name"),
                        rs.getDouble("amount"), rs.getString("expense_date"),
                        rs.getString("description"));
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return expense;
    }

    // Returns the total spent by the user in all stored months.
    public double getTotalSpent(int userId) throws SQLException {
        double total = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT IFNULL(SUM(amount), 0) AS total "
                    + "FROM expenses WHERE user_id = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return total;
    }

    // Returns month -> total spent using GROUP BY and SUM.
    public HashMap<String, Double> getMonthlySpentMap(int userId) throws SQLException {
        HashMap<String, Double> map = new HashMap<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT DATE_FORMAT(expense_date, '%Y-%m') AS month, "
                    + "SUM(amount) AS total FROM expenses WHERE user_id = ? "
                    + "GROUP BY DATE_FORMAT(expense_date, '%Y-%m')");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("month"), rs.getDouble("total"));
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return map;
    }

    // Checks if the user has expenses or income older than 5 months.
    public boolean hasOldData(int userId) throws SQLException {
        boolean found = false;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT (SELECT COUNT(*) FROM expenses WHERE user_id = ? "
                    + "AND expense_date < DATE_SUB(CURDATE(), INTERVAL 5 MONTH)) "
                    + "+ (SELECT COUNT(*) FROM monthly_income WHERE user_id = ? "
                    + "AND month_year < DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), "
                    + "'%Y-%m')) AS old_rows");
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                found = rs.getInt("old_rows") > 0;
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return found;
    }

    // Calls the stored procedure to delete old expenses and income.
    public void deleteOldData(int userId) throws SQLException {
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            cs = con.prepareCall("{call sp_delete_old_data(?)}");
            cs.setInt(1, userId);
            cs.execute();
            con.commit();
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.out.println("Rollback failed.");
                }
            }
            throw e;
        } finally {
            DBConnection.closeAll(con, cs, null);
        }
    }

    // Checks the amount, date and description of an expense.
    private void checkExpenseInput(double amount, String date, String description)
            throws InvalidInputException {
        if (amount <= 0) {
            throw new InvalidInputException("Amount must be greater than zero.");
        }
        if (!isValidDate(date)) {
            throw new InvalidInputException("Date must be in yyyy-MM-dd format.");
        }
        if (description.trim().isEmpty()) {
            throw new InvalidInputException("Description cannot be empty.");
        }
    }

    // Checks the date text with simple String methods.
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
}
