package model;

// An expense category. A default category is shared by all users.
public class Category {

    // OOP: Encapsulation - fields are private.
    private int categoryId;
    private String categoryName;
    private int userId;
    private boolean isDefault;

    // Empty constructor.
    public Category() {
    }

    // Constructor used when reading a category from the database.
    public Category(int categoryId, String categoryName, int userId, boolean isDefault) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.userId = userId;
        this.isDefault = isDefault;
    }

    // Returns the category id.
    public int getCategoryId() {
        return categoryId;
    }

    // Sets the category id.
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    // Returns the category name.
    public String getCategoryName() {
        return categoryName;
    }

    // Sets the category name.
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // Returns the owner user id (0 means a default category).
    public int getUserId() {
        return userId;
    }

    // Sets the owner user id.
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Tells if this is a default category.
    public boolean isDefault() {
        return isDefault;
    }

    // Sets the default flag.
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    // OOP: toString() from the Object class is overridden.
    // The category dropdown shows this text.
    @Override
    public String toString() {
        return categoryName;
    }
}
