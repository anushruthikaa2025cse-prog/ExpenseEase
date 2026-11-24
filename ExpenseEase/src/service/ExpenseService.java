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
                    + "AND expense_date < DATE_SUB(CURDATE(), INTERVAL 6 MONTH)) "
                    + "+ (SELECT COUNT(*) FROM monthly_income WHERE user_id = ? "
                    + "AND month_year < DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 MONTH), "
                    + "'%Y-%m')) "
                    + "+ (SELECT COUNT(*) FROM extra_income WHERE user_id = ? "
                    + "AND income_date < DATE_SUB(CURDATE(), INTERVAL 6 MONTH)) "
                    + "+ (SELECT COUNT(*) FROM monthly_budgets WHERE user_id = ? "
                    + "AND month_year < DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 MONTH), "
                    + "'%Y-%m')) AS old_rows");
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
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

    // Sets or updates the monthly budget of one user.
    public void setBudget(int userId, String monthYear, double amount)
            throws SQLException, InvalidInputException {
        if (amount < 0) {
            throw new InvalidInputException("Budget cannot be negative.");
        }
        if (monthYear.length() != 7) {
            throw new InvalidInputException("Month must be in yyyy-MM format.");
        }
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT budget_id FROM monthly_budgets "
                    + "WHERE user_id = ? AND month_year = ?");
            ps.setInt(1, userId);
            ps.setString(2, monthYear);
            rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close();
            ps.close();
            if (exists) {
                ps = con.prepareStatement(
                        "UPDATE monthly_budgets SET budget_amount = ? "
                        + "WHERE user_id = ? AND month_year = ?");
                ps.setDouble(1, amount);
                ps.setInt(2, userId);
                ps.setString(3, monthYear);
            } else {
                ps = con.prepareStatement(
                        "INSERT INTO monthly_budgets (user_id, month_year, budget_amount) "
                        + "VALUES (?, ?, ?)");
                ps.setInt(1, userId);
                ps.setString(2, monthYear);
                ps.setDouble(3, amount);
            }
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
    }

    // Returns the budget of one month, or zero if it is not set.
    public double getBudget(int userId, String monthYear) throws SQLException {
        double amount = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT budget_amount FROM monthly_budgets "
                    + "WHERE user_id = ? AND month_year = ?");
            ps.setInt(1, userId);
            ps.setString(2, monthYear);
            rs = ps.executeQuery();
            if (rs.next()) {
                amount = rs.getDouble("budget_amount");
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return amount;
    }

    // Sets or updates the savings goal of one user.
    public void setSavingsGoal(int userId, double target)
            throws SQLException, InvalidInputException {
        if (target < 0) {
            throw new InvalidInputException("Target cannot be negative.");
        }
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT goal_id FROM savings_goals WHERE user_id = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close();
            ps.close();
            if (exists) {
                ps = con.prepareStatement(
                        "UPDATE savings_goals SET target_amount = ? WHERE user_id = ?");
                ps.setDouble(1, target);
                ps.setInt(2, userId);
            } else {
                ps = con.prepareStatement(
                        "INSERT INTO savings_goals (user_id, target_amount) VALUES (?, ?)");
                ps.setInt(1, userId);
                ps.setDouble(2, target);
            }
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
    }

    // Returns the savings goal target, or zero if it is not set.
    public double getSavingsGoal(int userId) throws SQLException {
        double target = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT target_amount FROM savings_goals WHERE user_id = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                target = rs.getDouble("target_amount");
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return target;
    }

    // Returns expenses after applying search, filter and sort.
    public ArrayList<Expense> searchExpenses(int userId, String keyword,
                                            int categoryId, String month,
                                            String sortBy, boolean ascending)
            throws SQLException {
        ArrayList<Expense> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            String sql = "SELECT expense_id, user_id, category_id, category_name, "
                    + "amount, expense_date, description "
                    + "FROM expense_details_view WHERE user_id = ?";
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql = sql + " AND description LIKE ?";
            }
            if (categoryId > 0) {
                sql = sql + " AND category_id = ?";
            }
            if (month != null && !month.trim().isEmpty() && !month.equals("All")) {
                sql = sql + " AND DATE_FORMAT(expense_date, '%Y-%m') = ?";
            }
            String orderColumn = "expense_date";
            if ("Amount".equals(sortBy)) {
                orderColumn = "amount";
            }
            String direction = ascending ? "ASC" : "DESC";
            if ("Date".equals(sortBy) && !ascending) {
                sql = sql + " ORDER BY expense_date DESC, expense_id DESC";
            } else if ("Date".equals(sortBy)) {
                sql = sql + " ORDER BY expense_date ASC, expense_id ASC";
            } else if ("Amount".equals(sortBy) && ascending) {
                sql = sql + " ORDER BY amount ASC, expense_id ASC";
            } else {
                sql = sql + " ORDER BY amount DESC, expense_id DESC";
            }
            // Keep direction variable used to avoid warning.
            if (direction.length() == 0) {
            }
            ps = con.prepareStatement(sql);
            int index = 1;
            ps.setInt(index++, userId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(index++, "%" + keyword.trim() + "%");
            }
            if (categoryId > 0) {
                ps.setInt(index++, categoryId);
            }
            if (month != null && !month.trim().isEmpty() && !month.equals("All")) {
                ps.setString(index++, month.trim());
            }
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

    // Returns category -> total spent for one user and month.
    public HashMap<String, Double> getCategoryTotals(int userId, String month)
            throws SQLException {
        HashMap<String, Double> map = new HashMap<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            String sql = "SELECT category_name, SUM(amount) AS total "
                    + "FROM expense_details_view WHERE user_id = ?";
            if (month != null && !month.trim().isEmpty() && !month.equals("All")) {
                sql = sql + " AND DATE_FORMAT(expense_date, '%Y-%m') = ?";
            }
            sql = sql + " GROUP BY category_name";
            ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            if (month != null && !month.trim().isEmpty() && !month.equals("All")) {
                ps.setString(2, month.trim());
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("category_name"), rs.getDouble("total"));
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return map;
    }

    // Returns deleted expense logs of one user.
    public ArrayList<HashMap<String, String>> getDeletedLogs(int userId)
            throws SQLException {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT el.log_id, el.expense_id, el.category_id, c.category_name, "
                    + "el.amount, el.expense_date, el.description, el.deleted_at, "
                    + "el.restoration_status "
                    + "FROM expense_log el JOIN categories c "
                    + "ON el.category_id = c.category_id "
                    + "WHERE el.user_id = ? ORDER BY el.log_id DESC");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                HashMap<String, String> row = new HashMap<>();
                row.put("logId", String.valueOf(rs.getInt("log_id")));
                row.put("expenseId", String.valueOf(rs.getInt("expense_id")));
                row.put("categoryId", String.valueOf(rs.getInt("category_id")));
                row.put("categoryName", rs.getString("category_name"));
                row.put("amount", String.valueOf(rs.getDouble("amount")));
                row.put("date", rs.getString("expense_date"));
                row.put("description", rs.getString("description"));
                row.put("deletedAt", rs.getString("deleted_at"));
                row.put("status", rs.getString("restoration_status"));
                list.add(row);
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return list;
    }

    // Restores one deleted expense.
    public void restoreDeletedExpense(int userId, int logId)
            throws SQLException, InvalidInputException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            ps = con.prepareStatement(
                    "SELECT expense_id, category_id, amount, expense_date, "
                    + "description, restoration_status "
                    + "FROM expense_log WHERE log_id = ? AND user_id = ?");
            ps.setInt(1, logId);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new InvalidInputException("Deleted expense not found.");
            }
            if ("RESTORED".equals(rs.getString("restoration_status"))) {
                throw new InvalidInputException("This expense was already restored.");
            }
            int categoryId = rs.getInt("category_id");
            double amount = rs.getDouble("amount");
            String date = rs.getString("expense_date");
            String description = rs.getString("description");
            rs.close();
            ps.close();

            ps = con.prepareStatement(
                    "INSERT INTO expenses (user_id, category_id, amount, "
                    + "expense_date, description) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setDouble(3, amount);
            ps.setString(4, date);
            ps.setString(5, description);
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement(
                    "UPDATE expense_log SET restoration_status = 'RESTORED' "
                    + "WHERE log_id = ?");
            ps.setInt(1, logId);
            ps.executeUpdate();
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
            DBConnection.closeAll(con, ps, rs);
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
