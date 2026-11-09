package service;

import exception.InvalidInputException;
import exception.LoginFailedException;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Handles register, login and forgot password.
public class UserService {

    // Registers a new user. Uses commit and rollback.
    public void register(String username, String password, String email,
                         String securityAnswer)
            throws SQLException, InvalidInputException {
        checkRegisterInput(username, password, email, securityAnswer);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            if (usernameExists(con, username)) {
                throw new InvalidInputException("This username is already taken.");
            }
            ps = con.prepareStatement(
                    "INSERT INTO users (username, password, email, security_answer) "
                    + "VALUES (?, ?, ?, ?)");
            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, email.trim());
            ps.setString(4, securityAnswer.trim());
            ps.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw e;
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }

    // Checks if a username is already in the users table.
    private boolean usernameExists(Connection con, String username)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT user_id FROM users WHERE username = ?");
            ps.setString(1, username.trim());
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            DBConnection.closeAll(null, ps, rs);
        }
    }

    // Rolls back a transaction if the connection is open.
    private void rollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException e) {
                System.out.println("Rollback failed.");
            }
        }
    }

    // Checks the username and password and returns the user.
    public User login(String username, String password)
            throws SQLException, LoginFailedException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT user_id, username, password, email, security_answer "
                    + "FROM users WHERE username = ? AND password = ?");
            ps.setString(1, username.trim());
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("email"),
                        rs.getString("security_answer"));
            }
            throw new LoginFailedException("Wrong username or password.");
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
    }

    // Changes the password after checking username, email and answer.
    public void forgotPassword(String username, String email, String securityAnswer,
                               String newPassword)
            throws SQLException, LoginFailedException, InvalidInputException {
        checkForgotInput(username, email, securityAnswer, newPassword);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT user_id FROM users WHERE username = ? AND email = ? "
                    + "AND security_answer = ?");
            ps.setString(1, username.trim());
            ps.setString(2, email.trim());
            ps.setString(3, securityAnswer.trim());
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new LoginFailedException(
                        "Username, email or security answer is wrong.");
            }
            int userId = rs.getInt("user_id");
            ps.close();
            ps = con.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?");
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
    }

    // Checks that the register fields are filled properly.
    private void checkRegisterInput(String username, String password, String email,
                                    String securityAnswer) throws InvalidInputException {
        if (username.trim().isEmpty() || password.trim().isEmpty()
                || email.trim().isEmpty() || securityAnswer.trim().isEmpty()) {
            throw new InvalidInputException("Please fill all the fields.");
        }
        if (!email.contains("@")) {
            throw new InvalidInputException("Please enter a valid email.");
        }
    }

    // Checks that the forgot password fields are filled properly.
    private void checkForgotInput(String username, String email, String securityAnswer,
                                  String newPassword) throws InvalidInputException {
        if (username.trim().isEmpty() || email.trim().isEmpty()
                || securityAnswer.trim().isEmpty() || newPassword.trim().isEmpty()) {
            throw new InvalidInputException("Please fill all the fields.");
        }
    }
}
