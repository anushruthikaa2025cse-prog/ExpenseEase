package service;

import exception.InvalidInputException;
import model.IncomeEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

// Handles the monthly income of a user.
public class IncomeService {

    // Adds or updates the income of one month.
    public void setMonthlyIncome(int userId, String monthYear, double amount)
            throws SQLException, InvalidInputException {
        if (amount <= 0) {
            throw new InvalidInputException("Income must be greater than zero.");
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
                    "SELECT income_id FROM monthly_income "
                    + "WHERE user_id = ? AND month_year = ?");
            ps.setInt(1, userId);
            ps.setString(2, monthYear);
            rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close();
            ps.close();
            if (exists) {
                ps = con.prepareStatement(
                        "UPDATE monthly_income SET amount = ? "
                        + "WHERE user_id = ? AND month_year = ?");
                ps.setDouble(1, amount);
                ps.setInt(2, userId);
                ps.setString(3, monthYear);
            } else {
                ps = con.prepareStatement(
                        "INSERT INTO monthly_income (user_id, month_year, amount) "
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

    // Returns the income of one month, or zero if it is not set.
    public double getIncome(int userId, String monthYear) throws SQLException {
        double amount = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT amount FROM monthly_income "
                    + "WHERE user_id = ? AND month_year = ?");
            ps.setInt(1, userId);
            ps.setString(2, monthYear);
            rs = ps.executeQuery();
            if (rs.next()) {
                amount = rs.getDouble("amount");
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return amount;
    }

    // Returns all months and incomes in a HashMap (month -> amount).
    public HashMap<String, Double> getMonthlyIncomeMap(int userId) throws SQLException {
        HashMap<String, Double> map = new HashMap<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT month_year, amount FROM monthly_income WHERE user_id = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("month_year"), rs.getDouble("amount"));
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return map;
    }

    // Returns the total income of all stored months.
    public double getTotalIncome(int userId) throws SQLException {
        double total = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT IFNULL(SUM(amount), 0) AS total "
                    + "FROM monthly_income WHERE user_id = ?");
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

    // Adds one extra income entry.
    public void addExtraIncome(int userId, double amount, String date,
                               String incomeType, String description)
            throws SQLException, InvalidInputException {
        if (amount <= 0) {
            throw new InvalidInputException("Amount must be greater than zero.");
        }
        if (!isValidDate(date)) {
            throw new InvalidInputException("Date must be in yyyy-MM-dd format.");
        }
        if (incomeType.trim().isEmpty()) {
            throw new InvalidInputException("Income type cannot be empty.");
        }
        if (description.trim().isEmpty()) {
            description = incomeType.trim();
        }
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "INSERT INTO extra_income (user_id, amount, income_date, "
                    + "income_type, description) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, userId);
            ps.setDouble(2, amount);
            ps.setString(3, date);
            ps.setString(4, incomeType.trim());
            ps.setString(5, description.trim());
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }

    // Returns the total of all extra income entries.
    public double getTotalExtraIncome(int userId) throws SQLException {
        double total = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT IFNULL(SUM(amount), 0) AS total "
                    + "FROM extra_income WHERE user_id = ?");
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

    // Returns month -> total extra income.
    public HashMap<String, Double> getMonthlyExtraIncomeMap(int userId)
            throws SQLException {
        HashMap<String, Double> map = new HashMap<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT DATE_FORMAT(income_date, '%Y-%m') AS month, "
                    + "SUM(amount) AS total FROM extra_income WHERE user_id = ? "
                    + "GROUP BY DATE_FORMAT(income_date, '%Y-%m')");
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

    // Returns all extra income entries of one user, newest first.
    public ArrayList<IncomeEntry> listExtraIncome(int userId) throws SQLException {
        ArrayList<IncomeEntry> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT extra_income_id, user_id, amount, income_date, "
                    + "income_type, description FROM extra_income "
                    + "WHERE user_id = ? ORDER BY income_date DESC, extra_income_id DESC");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new IncomeEntry(rs.getInt("extra_income_id"),
                        rs.getInt("user_id"), rs.getDouble("amount"),
                        rs.getString("income_date"), rs.getString("income_type"),
                        rs.getString("description")));
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return list;
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
}
