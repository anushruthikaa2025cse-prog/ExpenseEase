-- ExpenseEase - database schema
-- Run: mysql -u root -p < sql/schema.sql

DROP DATABASE IF EXISTS expense_tracker;
CREATE DATABASE expense_tracker;
USE expense_tracker;

-- 1. Users
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    security_answer VARCHAR(100) NOT NULL
);

-- 2. Categories (user_id NULL means shared default category)
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    user_id INT NULL,
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_category_default CHECK (is_default IN (0, 1)),
    CONSTRAINT uq_category_user_name UNIQUE (user_id, category_name)
);

-- 3. Expenses
CREATE TABLE expenses (
    expense_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(200) NOT NULL,
    CONSTRAINT chk_expense_amount CHECK (amount > 0),
    CONSTRAINT fk_expense_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_id)
        REFERENCES categories(category_id)
);

-- 4. Monthly salary/income
CREATE TABLE monthly_income (
    income_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    month_year VARCHAR(7) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_monthly_income_amount CHECK (amount > 0),
    CONSTRAINT uq_monthly_income_user_month UNIQUE (user_id, month_year),
    CONSTRAINT fk_monthly_income_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. Extra income entries
CREATE TABLE extra_income (
    extra_income_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    income_date DATE NOT NULL,
    income_type VARCHAR(50) NOT NULL,
    description VARCHAR(200) NOT NULL,
    CONSTRAINT chk_extra_income_amount CHECK (amount > 0),
    CONSTRAINT fk_extra_income_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- 6. Monthly budgets
CREATE TABLE monthly_budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    month_year VARCHAR(7) NOT NULL,
    budget_amount DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_budget_amount CHECK (budget_amount >= 0),
    CONSTRAINT uq_budget_user_month UNIQUE (user_id, month_year),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- 7. Savings goals (one per user)
CREATE TABLE savings_goals (
    goal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    target_amount DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_goal_amount CHECK (target_amount >= 0),
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- 8. Recurring transactions (salary, expense, EMI)
CREATE TABLE recurring_transactions (
    recurring_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    category_id INT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    frequency_months INT NOT NULL,
    start_date DATE NOT NULL,
    next_due_date DATE NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    payments_total INT NOT NULL DEFAULT 0,
    payments_done INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_recurring_type CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'EMI')),
    CONSTRAINT chk_recurring_amount CHECK (amount > 0),
    CONSTRAINT chk_recurring_frequency CHECK (frequency_months > 0),
    CONSTRAINT chk_recurring_active CHECK (is_active IN (0, 1)),
    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_category FOREIGN KEY (category_id)
        REFERENCES categories(category_id)
);

-- 9. Deleted expense history (trigger writes here)
CREATE TABLE expense_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    expense_id INT NOT NULL,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(200) NOT NULL,
    deleted_at DATE NOT NULL,
    restoration_status VARCHAR(20) NOT NULL DEFAULT 'DELETED',
    CONSTRAINT fk_log_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- View
CREATE VIEW expense_details_view AS
SELECT e.expense_id,
       e.user_id,
       e.category_id,
       c.category_name,
       e.amount,
       e.expense_date,
       e.description
FROM expenses e
JOIN categories c ON e.category_id = c.category_id;

-- Trigger: copy deleted expense to expense_log
DELIMITER //
CREATE TRIGGER trg_expense_after_delete
AFTER DELETE ON expenses
FOR EACH ROW
BEGIN
    INSERT INTO expense_log (expense_id, user_id, category_id, amount,
                             expense_date, description, deleted_at, restoration_status)
    VALUES (OLD.expense_id, OLD.user_id, OLD.category_id, OLD.amount,
            OLD.expense_date, OLD.description, CURDATE(), 'DELETED');
END //
DELIMITER ;

-- Stored procedure: delete data older than 6 months
DELIMITER //
CREATE PROCEDURE sp_delete_old_data(IN p_user_id INT)
BEGIN
    DELETE FROM expenses
    WHERE user_id = p_user_id
      AND expense_date < DATE_SUB(CURDATE(), INTERVAL 6 MONTH);

    DELETE FROM monthly_income
    WHERE user_id = p_user_id
      AND month_year < DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 MONTH), '%Y-%m');

    DELETE FROM extra_income
    WHERE user_id = p_user_id
      AND income_date < DATE_SUB(CURDATE(), INTERVAL 6 MONTH);

    DELETE FROM monthly_budgets
    WHERE user_id = p_user_id
      AND month_year < DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 MONTH), '%Y-%m');
END //
DELIMITER ;

-- Default categories
INSERT INTO categories (category_name, user_id, is_default) VALUES
('Food', NULL, 1),
('Fuel', NULL, 1),
('Subscriptions', NULL, 1),
('Rent', NULL, 1),
('Bills', NULL, 1),
('Shopping', NULL, 1),
('Travel', NULL, 1),
('Education', NULL, 1),
('Entertainment', NULL, 1),
('Health', NULL, 1),
('Other', NULL, 1);