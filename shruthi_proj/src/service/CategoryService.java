package service;

import exception.InvalidInputException;
import model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

// Handles default and custom categories.
public class CategoryService {

    // Returns default categories plus the user's own categories.
    public ArrayList<Category> listCategories(int userId) throws SQLException {
        ArrayList<Category> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "SELECT category_id, category_name, user_id, is_default "
                    + "FROM categories WHERE user_id IS NULL OR user_id = ? "
                    + "ORDER BY is_default DESC, category_name");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Category(rs.getInt("category_id"),
                        rs.getString("category_name"), rs.getInt("user_id"),
                        rs.getBoolean("is_default")));
            }
        } finally {
            DBConnection.closeAll(con, ps, rs);
        }
        return list;
    }

    // Adds a custom category for one user.
    public void addCustomCategory(int userId, String name)
            throws SQLException, InvalidInputException {
        String cleanName = name.trim();
        if (cleanName.isEmpty()) {
            throw new InvalidInputException("Category name cannot be empty.");
        }
        // Collections: HashSet is used to find duplicate names quickly.
        HashSet<String> names = new HashSet<>();
        for (Category category : listCategories(userId)) {
            names.add(category.getCategoryName().toLowerCase());
        }
        if (names.contains(cleanName.toLowerCase())) {
            throw new InvalidInputException("This category already exists.");
        }
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "INSERT INTO categories (category_name, user_id, is_default) "
                    + "VALUES (?, ?, 0)");
            ps.setString(1, cleanName);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } finally {
            DBConnection.closeAll(con, ps, null);
        }
    }
}
