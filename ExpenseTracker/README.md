# Expense Tracker (Java 21 + Swing + MySQL)

## Features
- Add expenses with amount, category, date and description
- List expenses newest first
- Show total spent, today's spending and number of transactions
- Delete a selected expense
- Validate amount and date
- Save data in MySQL

## Requirements
- JDK 21
- MySQL Server and MySQL Workbench (or another SQL client)
- MySQL Connector/J JAR

## 1. Create the database
Open MySQL Workbench, open `schema.sql`, and execute it.

## 2. Download JDBC driver
Download MySQL Connector/J from the official MySQL website:
https://dev.mysql.com/downloads/connector/j/

Choose the platform-independent archive, extract it, and copy the `mysql-connector-j-<version>.jar`
file into this project's `lib` folder. Rename the copied file to:
`mysql-connector-j.jar`

The final path should be `lib/mysql-connector-j.jar`.

## 3. Set your MySQL password
Open `src/ExpenseTracker.java`. Change:
`private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";`
to your MySQL password. Also change `DB_USER` if your account is not `root`.

Do not upload a real password to GitHub or share it publicly.

## 4. Build and run on Windows
Open PowerShell in this project folder and run:
```powershell
.\build.bat
.\run.bat
```
Make sure MySQL Server is running first.

## 5. Package as a runnable JAR
After adding the driver JAR, run:
```powershell
.\package.bat
```
This creates `dist/ExpenseTracker.jar`. Keep the `lib` folder beside the JAR because the JAR manifest references the JDBC driver there. Run it with:
```powershell
java -jar dist\ExpenseTracker.jar
```
Run from the project folder so the relative `lib` path resolves.

## 6. Package as a Windows EXE (optional)
Install a JDK that includes `jpackage` (JDK 21 normally does). From the project folder, run:
```powershell
.\package-exe.bat
```
This creates a Windows app image in `dist\ExpenseTrackerApp`. For an installer, add `--type exe` to the `jpackage` command; creating an EXE installer may require WiX Toolset depending on your JDK packaging setup.

## Notes
- This is a local desktop application. MySQL Server must be installed and running.
- The example uses a local database account for beginner setup. For a real distributed app, do not embed database credentials in a client application; use a backend service instead.
