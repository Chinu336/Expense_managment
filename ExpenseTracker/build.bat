@echo off
setlocal
if not exist "lib\mysql-connector-j.jar" (
  echo ERROR: Put the MySQL Connector/J JAR at lib\mysql-connector-j.jar
  exit /b 1
)
if not exist "out" mkdir out
javac -encoding UTF-8 -cp "lib\mysql-connector-j.jar" -d out src\ExpenseTracker.java
if errorlevel 1 exit /b 1
echo Build successful.
