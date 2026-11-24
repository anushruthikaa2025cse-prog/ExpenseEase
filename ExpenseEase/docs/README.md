# ExpenseEase (Java Swing + MySQL)

ExpenseEase is a desktop personal expense tracker built with plain Java, Java Swing,
MySQL, JDBC and Apache POI. It is designed as a first-year CSE college project
with a clean, professional light-green user interface.

## Features

- User registration, login, forgot password and account settings.
- Monthly income and extra income management.
- Recurring salary, recurring expenses and EMI with pause/resume.
- Add, edit and delete expenses with a calendar date picker.
- Custom user-specific expense categories.
- Monthly budgets with 80% and 100% warnings.
- Savings goals with progress.
- Search, filter and sort expenses.
- Category-wise and month-wise reports.
- Deleted expense history with restore.
- Statistics dashboard with charts and insight sentences.
- Export to CSV, XML and Excel (.xlsx).
- Six-month data retention using a stored procedure.

## Requirements

- JDK 8 or newer.
- MySQL Server 8.x.
- MySQL Connector/J and Apache POI jars in `lib/`.

## Database setup

1. Start the MySQL server (Windows service `MySQL80`):

   ```bat
   net start MySQL80
   ```

   Or start it from the Services application. MySQL must be running before you
   start ExpenseEase, otherwise the connection will fail.

2. Create the database and load the sample data:

   ```bat
   mysql -u root -p < sql\schema.sql
   mysql -u root -p < sql\sample_data.sql
   ```

   In PowerShell, use `Get-Content` instead of `<`:

   ```powershell
   Get-Content sql\schema.sql | mysql -u root -p
   Get-Content sql\sample_data.sql | mysql -u root -p
   ```

3. Update `db.properties` with your MySQL password:

   ```properties
   db.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=your_mysql_password
   ```

   Leave `db.password` empty only if your MySQL root account has no password.
   You can also override these values with environment variables (`DB_URL`,
   `DB_USER`, `DB_PASSWORD`) or JVM system properties
   (`-Dexpenseease.db.url=...`, `-Dexpenseease.db.user=...`,
   `-Dexpenseease.db.password=...`).

4. Test the connection without opening the GUI:

   ```bat
   java -cp "out;lib\*" service.DBConnection
   ```

   You should see `Connected to MySQL 8.0.x`.

## Compile (Windows)

```bat
mkdir out
javac -cp "lib\*" -d out src\model\*.java src\exception\*.java src\service\*.java src\ui\*.java src\Main.java
```

## Run (Windows)

```bat
java -cp "out;lib\*" Main
```

Always run these commands from the project root so that `db.properties` and the
`lib/` folder can be found. If you run from another directory, set
`DB_PASSWORD` (and the other `DB_*` variables) instead.

## Troubleshooting the database connection

- **"Communications link failure"** - MySQL is not running. Start the service
  with `net start MySQL80` (run the command prompt as Administrator) and try
  again.
- **"Access denied for user 'root'"** - the password in `db.properties` does
  not match your MySQL root password. Update it and restart the application.
- **"Unknown database 'expense_tracker'"** - run `sql\schema.sql` first.
- **"MySQL driver not found"** - make sure `lib\mysql-connector-j-8.4.0.jar`
  is on the classpath (use `lib\*`).
- **Running from a different folder** - `db.properties` is read from the
  current working directory first, then from the classpath and your home
  directory. Use the `DB_*` environment variables if needed.

## Project structure

- `src/` : Java source files.
- `sql/` : schema and sample data.
- `lib/` : required JAR files.
- `docs/` : README and viva notes.

## Notes

- Passwords are stored as plain text because hashing is not in the syllabus.
- Money values use `double` in Java and `DECIMAL(10,2)` in MySQL.
- Dates are stored as Strings in `yyyy-MM-dd` format.
- Recurring transactions are processed when the application is opened.