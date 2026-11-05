-- Sample data for testing. Run after schema.sql.
-- Passwords are plain text because hashing is not in the syllabus.

USE expense_tracker;

INSERT INTO users (username, password, email, security_answer) VALUES
('aditi', 'pass123', 'aditi@example.com', 'Fluffy'),
('shruthi', 'pass123', 'shruthi@example.com', 'Tommy');

-- One custom category for user 1 (aditi).
INSERT INTO categories (category_name, user_id, is_default) VALUES
('Gifts', 1, 0);

-- Monthly income for aditi.
INSERT INTO monthly_income (user_id, month_year, amount) VALUES
(1, '2026-10', 30000),
(1, '2026-11', 32000);

-- Expenses for aditi. Category ids come from schema.sql:
-- 1 Food, 2 Fuel, 3 Subscriptions, 4 Rent, 5 Bills, 6 Shopping,
-- 7 Travel, 8 Education, 9 Entertainment, 10 Health, 11 Other, 12 Gifts.
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

-- An old row (older than 5 months) to test the auto-delete on login.
INSERT INTO expenses (user_id, category_id, amount, expense_date, description) VALUES
(1, 1, 900, '2026-01-15', 'Old expense');
INSERT INTO monthly_income (user_id, month_year, amount) VALUES
(1, '2026-01', 20000);

-- A few rows for user 2 (shruthi).
INSERT INTO monthly_income (user_id, month_year, amount) VALUES
(2, '2026-11', 25000);

INSERT INTO expenses (user_id, category_id, amount, expense_date, description) VALUES
(2, 1, 500, '2026-11-01', 'Mess fees'),
(2, 7, 300, '2026-11-01', 'Bus ticket');
