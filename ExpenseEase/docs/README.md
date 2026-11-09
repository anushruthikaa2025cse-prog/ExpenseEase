# ExpenseEase (Java Swing + MySQL)

A simple desktop expense tracker for a first-year CSE student. It uses plain
Java, Swing and MySQL JDBC. There is no Maven, no Gradle, no framework and no
external library other than the MySQL connector jar in `lib/`.

## 1. What the app can do

1. Register with username, password, email and a security answer.
2. Login with username and password.
3. Forgot password using username + email + security answer.
4. Set or update the monthly income.
5. Add an expense (category, amount, date, description).
6. Add a custom category for the logged-in user.
7. Edit or delete an expense selected in the table.
8. Keep only the latest 5 months of data (old data is deleted with a stored
   procedure after asking the user to export first).
9. Export all data to an Excel (`.xlsx`) file, month by month.
10. Logout.

The dashboard shows total income, total spent, savings and all expenses in a
table. The table and the three cards refresh after every add, edit, delete or
income change.

## 2. Requirements

- JDK 8 or newer (`java` and `javac`)
- MySQL Server 8.x
- MySQL Connector/J jar (already in `lib/mysql-connector-j-8.4.0.jar`)

The project was tested with JDK 26 and MySQL 8.0.46.

## 3. Project files (27 files)

```text
src/Main.java                       starts the application
src/model/User.java                 user data
src/model/Expense.java              expense data
src/model/Category.java             category data
src/exception/InvalidInputException.java
src/exception/LoginFailedException.java
src/service/DBConnection.java       database settings and connection
src/service/UserService.java        register, login, forgot password
src/service/ExpenseService.java     add, edit, delete, list, 5-month delete
src/service/CategoryService.java    default and custom categories
src/service/IncomeService.java      monthly income
src/service/Exportable.java         interface (default + static method)
src/service/XlsxExporter.java       writes the Excel report
src/ui/AppColors.java               light green colour palette
src/ui/BasePanel.java               abstract parent of every screen
src/ui/LoginPanel.java              login screen
src/ui/RegisterPanel.java           register screen
src/ui/ForgotPasswordPanel.java     forgot password screen
src/ui/DashboardPanel.java          main screen after login
src/ui/ExpenseDialog.java           add/edit expense dialog
src/ui/MainFrame.java               CardLayout window
sql/schema.sql                      5 tables + trigger + procedure + view
sql/sample_data.sql                 sample users and rows
db.properties                       local MySQL URL, user and password
lib/mysql-connector-j-8.4.0.jar     MySQL JDBC driver
docs/README.md                      this file
docs/VIVA_NOTES.md                  viva preparation notes
```

## 4. Create the database

Open a terminal in the project folder.

### Windows Command Prompt

```bat
mysql -u root -p < sql\schema.sql
mysql -u root -p < sql\sample_data.sql
```

### Windows PowerShell

PowerShell does not support `<` redirection, so use `cmd /c`:

```powershell
cmd /c "mysql -u root -p < sql\schema.sql"
cmd /c "mysql -u root -p < sql\sample_data.sql"
```

### Linux / macOS

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/sample_data.sql
```

`schema.sql` drops and recreates the `expense_tracker` database. It creates
the 5 tables, the trigger, the stored procedure, the view and the 11 default
categories. `sample_data.sql` is optional and adds two test users:

| Username | Password | Security answer |
| --- | --- | --- |
| `aditi` | `pass123` | `Fluffy` |
| `shruthi` | `pass123` | `Tommy` |

## 5. Set your MySQL password

The connection settings are read from `db.properties` in the project folder:

```properties
db.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root
```

Change `db.password` to your MySQL root password. The database name is
`expense_tracker`, and the default MySQL port is `3306`.

Settings are chosen in this order (first one wins):

1. Java system property, e.g. `-Dexpenseease.db.password=secret`
2. Environment variable, e.g. `DB_PASSWORD`
3. `db.properties`
4. Built-in defaults (`root` / `root`)

Test the connection on its own before starting the app:

```bat
java -cp "out;lib\mysql-connector-j-8.4.0.jar" service.DBConnection
```

It should print `Connected to MySQL 8.1.0` (or your MySQL version).

## 6. Compile

Create an `out` folder first, then compile.

### Windows

```bat
mkdir out
javac -cp "lib\mysql-connector-j-8.4.0.jar" -d out src\model\*.java src\exception\*.java src\service\*.java src\ui\*.java src\Main.java
```

### Linux / macOS

```bash
mkdir -p out
javac -cp "lib/mysql-connector-j-8.4.0.jar" -d out src/model/*.java src/exception/*.java src/service/*.java src/ui/*.java src/Main.java
```

## 7. Run

### Windows

```bat
java -cp "out;lib\mysql-connector-j-8.4.0.jar" Main
```

### Linux / macOS

```bash
java -cp "out:lib/mysql-connector-j-8.4.0.jar" Main
```

On Windows the classpath separator is `;`. On Linux/macOS it is `:`.

## 8. Screens

1. **Login** - username, password, links to register and forgot password.
2. **Register** - username, password, email, security answer.
3. **Forgot Password** - username, email, security answer, new password.
4. **Dashboard** - welcome bar, three summary cards, expense table and action
   buttons (Add, Edit, Delete, Add Category, Set/Update Income, Export, Logout).

The window uses one `MainFrame` with a `CardLayout` to switch between these
four screens.

## 9. How to test the full flow

1. Run `schema.sql` and `sample_data.sql`.
2. Start the app and login as `aditi` / `pass123`.
3. Because the sample data contains rows older than 5 months, the app asks:
   "Some old data will be deleted. Export it first?" Click **Yes** and save the
   Excel file, or click **No**. The stored procedure then deletes the old rows.
4. The current month's income already exists for `aditi` in the sample data.
   Use **Set/Update Monthly Income** to change it.
5. Click **Add Expense**, choose a category, enter an amount, a date like
   `2026-11-05` and a description. The table and cards update immediately.
6. Click **Add Category**, type a name such as `Gifts`. It appears in the
   category dropdown the next time you add or edit an expense.
7. Select a row and click **Edit Expense** to change it, or **Delete Expense**
   and confirm. The trigger writes the deleted row into `expense_log`.
8. Click **Export to Excel** and save the file. Open it in Excel or
   LibreOffice Calc. Each month is a separate block with its expenses, month
   total, income and savings.
9. Logout and test **Forgot Password** with the username, email and security
   answer. Set a new password and login again.
10. To test the 5-month deletion again, run `schema.sql` and `sample_data.sql`
    again. The sample data contains old rows from January 2026.

Useful SQL checks:

```sql
USE expense_tracker;
SELECT * FROM expense_details_view;
SELECT * FROM expense_log;
CALL sp_delete_old_data(1);
```

## 10. Database objects

- 5 tables: `users`, `categories`, `expenses`, `monthly_income`, `expense_log`
- Trigger: `trg_expense_after_delete` (AFTER DELETE on `expenses`)
- Procedure: `sp_delete_old_data(IN p_user_id INT)`
- View: `expense_details_view`
- Default categories: Food, Fuel, Subscriptions, Rent, Bills, Shopping, Travel,
  Education, Entertainment, Health, Other

The database is in 3NF: every non-key column depends only on the primary key
of its table. Category names are stored only in `categories`, usernames only in
`users`, and expenses refer to a category by `category_id`.

## 11. Notes

- Passwords are stored as plain text because password hashing is not part of
  the syllabus.
- Money values use `double` in Java and `DECIMAL(10,2)` in MySQL.
- Dates are `String` values in `yyyy-MM-dd` format and `DATE` columns in MySQL.
- The `.xlsx` file is built by hand. An XLSX file is a zip of small XML parts,
  so `XlsxExporter` writes the XML text with `StringBuffer` and packs it with
  `ZipOutputStream`. No Excel or XML library is used.
- Every normal query uses `PreparedStatement`. The stored procedure is called
  with `CallableStatement`.
- `out/` is only the compiled output folder. It is not part of the source.
