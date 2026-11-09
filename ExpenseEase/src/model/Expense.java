package model;

// One expense row of a user.
public class Expense {

    // OOP: Encapsulation - fields are private.
    private int expenseId;
    private int userId;
    private int categoryId;
    private String categoryName;
    private double amount;
    private String date;
    private String description;

    // Empty constructor.
    public Expense() {
    }

    // Constructor used when adding a new expense.
    public Expense(int userId, int categoryId, double amount,
                   String date, String description) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    // Constructor used when reading an expense from the database.
    public Expense(int expenseId, int userId, int categoryId, String categoryName,
                   double amount, String date, String description) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    // Returns the expense id.
    public int getExpenseId() {
        return expenseId;
    }

    // Sets the expense id.
    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    // Returns the user id.
    public int getUserId() {
        return userId;
    }

    // Sets the user id.
    public void setUserId(int userId) {
        this.userId = userId;
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

    // Returns the amount.
    public double getAmount() {
        return amount;
    }

    // Sets the amount.
    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Returns the date text (yyyy-MM-dd).
    public String getDate() {
        return date;
    }

    // Sets the date text.
    public void setDate(String date) {
        this.date = date;
    }

    // Returns the description.
    public String getDescription() {
        return description;
    }

    // Sets the description.
    public void setDescription(String description) {
        this.description = description;
    }

    // OOP: toString() from the Object class is overridden.
    @Override
    public String toString() {
        return date + " | " + categoryName + " | Rs. " + amount
                + " | " + description;
    }
}
