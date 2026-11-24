package model;

// A salary, expense or EMI that repeats every few months.
public class RecurringTransaction {

    // OOP: Encapsulation - fields are private.
    private int recurringId;
    private int userId;
    private String transactionType;
    private String title;
    private int categoryId;
    private String categoryName;
    private double amount;
    private int frequencyMonths;
    private String startDate;
    private String nextDueDate;
    private boolean active;
    private int paymentsTotal;
    private int paymentsDone;

    // Empty constructor.
    public RecurringTransaction() {
    }

    // Constructor used when reading from the database.
    public RecurringTransaction(int recurringId, int userId, String transactionType,
                               String title, int categoryId, String categoryName,
                               double amount, int frequencyMonths, String startDate,
                               String nextDueDate, boolean active, int paymentsTotal,
                               int paymentsDone) {
        this.recurringId = recurringId;
        this.userId = userId;
        this.transactionType = transactionType;
        this.title = title;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amount = amount;
        this.frequencyMonths = frequencyMonths;
        this.startDate = startDate;
        this.nextDueDate = nextDueDate;
        this.active = active;
        this.paymentsTotal = paymentsTotal;
        this.paymentsDone = paymentsDone;
    }

    public int getRecurringId() {
        return recurringId;
    }

    public void setRecurringId(int recurringId) {
        this.recurringId = recurringId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getFrequencyMonths() {
        return frequencyMonths;
    }

    public void setFrequencyMonths(int frequencyMonths) {
        this.frequencyMonths = frequencyMonths;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(String nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPaymentsTotal() {
        return paymentsTotal;
    }

    public void setPaymentsTotal(int paymentsTotal) {
        this.paymentsTotal = paymentsTotal;
    }

    public int getPaymentsDone() {
        return paymentsDone;
    }

    public void setPaymentsDone(int paymentsDone) {
        this.paymentsDone = paymentsDone;
    }

    // OOP: toString() from the Object class is overridden.
    @Override
    public String toString() {
        return title + " | " + transactionType + " | Rs. " + amount
                + " | next: " + nextDueDate;
    }
}