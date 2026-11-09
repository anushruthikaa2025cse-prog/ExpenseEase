-- ExpenseEase - database schema
-- Run this file first:  mysql -u root -p < sql/schema.sql
-- It drops the old database and creates a fresh one.

DROP DATABASE IF EXISTS expense_tracker;
CREATE DATABASE expense_tracker;
USE expense_tracker;

-- 1. Users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    security_answer VARCHAR(100) NOT NULL
);

-- 2. Categories table. user_id is NULL for the shared default categories.
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

-- 3. Expenses table
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

-- 4. Monthly income table. One row per user per month.
CREATE TABLE monthly_income (
    income_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    month_year VARCHAR(7) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_income_amount CHECK (amount > 0),
    CONSTRAINT uq_income_user_month UNIQUE (user_id, month_year),
    CONSTRAINT fk_income_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. Expense log table. The trigger below writes into it.
CREATE TABLE expense_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action_name VARCHAR(20) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    log_date DATE NOT NULL,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- One view that joins expenses and categories.
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

-- One trigger: every deleted expense is written to expense_log.
DELIMITER //
CREATE TRIGGER trg_expense_after_delete
AFTER DELETE ON expenses
FOR EACH ROW
BEGIN
    INSERT INTO expense_log (user_id, action_name, amount, log_date)
    VALUES (OLD.user_id, 'DELETE', OLD.amount, CURDATE());
END //
DELIMITER ;

-- One stored procedure: deletes data older than 5 months for one user.
DELIMITER //
CREATE PROCEDURE sp_delete_old_data(IN p_user_id INT)
BEGIN
    DELETE FROM expenses
    WHERE user_id = p_user_id
      AND expense_date < DATE_SUB(CURDATE(), INTERVAL 5 MONTH);

    DELETE FROM monthly_income
    WHERE user_id = p_user_id
      AND month_year < DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m');
END //
DELIMITER ;

-- Default categories shared by every user (user_id is NULL).
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
