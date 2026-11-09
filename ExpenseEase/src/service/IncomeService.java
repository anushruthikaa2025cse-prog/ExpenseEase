package service;

import exception.InvalidInputException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
