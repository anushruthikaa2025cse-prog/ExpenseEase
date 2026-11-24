-- Sample data for testing. Run after schema.sql.
USE expense_tracker;

INSERT INTO users (username, password, email, security_answer) VALUES
('aditi', 'pass123', 'aditi@example.com', 'Fluffy'),
('shruthi', 'pass123', 'shruthi@example.com', 'Tommy');

INSERT INTO categories (category_name, user_id, is_default) VALUES
('Gifts', 1, 0);

INSERT INTO monthly_income (user_id, month_year, amount) VALUES
(1, '2026-10', 30000),
(1, '2026-11', 32000);

INSERT INTO extra_income (user_id, amount, income_date, income_type, description) VALUES
(1, 5000, '2026-10-15', 'Bonus', 'Festival bonus');

INSERT INTO expenses (user_id, category_id, amount, expense_date, description) VALUES
(1, 1, 250, '2026-10-02', 'Lunch'),
(1, 1, 1200, '2026-10-08', 'Groceries'),
(1, 2, 500, '2026-10-10', 'Petrol'),
(1, 4, 7000, '2026-10-01', 'Hostel rent'),
(1, 5, 800, '2026-10-15', 'Electricity bill'),
(1, 9, 400, '2026-10-20', 'Movie'),
(1, 12, 600, '2026-10-25', 'Birthday gift'),
(1, 1, 300, '2026-11-01', 'Breakfast'),
(1, 7, 1500, '2026-11-01', 'Train ticket');

INSERT INTO expenses (user_id, category_id, amount, expense_date, description) VALUES
(1, 1, 900, '2026-01-15', 'Old expense');
INSERT INTO monthly_income (user_id, month_year, amount) VALUES
(1, '2026-01', 20000);

INSERT INTO monthly_budgets (user_id, month_year, budget_amount) VALUES
(1, '2026-10', 15000),
(1, '2026-11', 18000);

INSERT INTO savings_goals (user_id, target_amount) VALUES
(1, 10000);

INSERT INTO recurring_transactions (user_id, transaction_type, title, category_id, amount, frequency_months, start_date, next_due_date, is_active, payments_total, payments_done) VALUES
(1, 'INCOME', 'Monthly salary', NULL, 30000, 1, '2026-11-05', '2026-11-05', 1, 0, 0),
(1, 'EXPENSE', 'Hostel rent', 4, 7000, 1, '2026-11-01', '2026-11-01', 1, 0, 0),
(1, 'EMI', 'Laptop EMI', 6, 5000, 1, '2026-11-10', '2026-11-10', 1, 12, 0);

INSERT INTO monthly_income (user_id, month_year, amount) VALUES
(2, '2026-11', 25000);

INSERT INTO expenses (user_id, category_id, amount, expense_date, description) VALUES
(2, 1, 500, '2026-11-01', 'Mess fees'),
(2, 7, 300, '2026-11-01', 'Bus ticket');