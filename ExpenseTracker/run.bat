@echo off
setlocal
if not exist "out\ExpenseTracker.class" call build.bat
if errorlevel 1 exit /b 1
java -cp "out;lib\mysql-connector-j.jar" ExpenseTracker
