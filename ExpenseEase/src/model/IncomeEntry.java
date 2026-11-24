package model;

// One income entry. It can be monthly salary or extra income.
public class IncomeEntry {

    // OOP: Encapsulation - fields are private.
    private int incomeId;
    private int userId;
    private double amount;
    private String date;
    private String incomeType;
    private String description;

    // Empty constructor.
    public IncomeEntry() {
    }

    // Constructor used when adding a new income entry.
    public IncomeEntry(int userId, double amount, String date,
                       String incomeType, String description) {
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.incomeType = incomeType;
        this.description = description;
    }

    // Constructor used when reading an income entry from the database.
    public IncomeEntry(int incomeId, int userId, double amount, String date,
                       String incomeType, String description) {
        this.incomeId = incomeId;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.incomeType = incomeType;
        this.description = description;
    }

    public int getIncomeId() {
        return incomeId;
    }

    public void setIncomeId(int incomeId) {
        this.incomeId = incomeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIncomeType() {
        return incomeType;
    }

    public void setIncomeType(String incomeType) {
        this.incomeType = incomeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // OOP: toString() from the Object class is overridden.
    @Override
    public String toString() {
        return date + " | " + incomeType + " | Rs. " + amount
                + " | " + description;
    }
}