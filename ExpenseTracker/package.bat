@echo off
setlocal
call build.bat
if errorlevel 1 exit /b 1
if not exist "dist" mkdir dist
(
  echo Main-Class: ExpenseTracker
  echo Class-Path: ../lib/mysql-connector-j.jar
  echo.
) > manifest.mf
jar cfm dist\ExpenseTracker.jar manifest.mf -C out .
if errorlevel 1 exit /b 1
echo Created dist\ExpenseTracker.jar
echo Keep the lib folder beside dist so the JDBC driver can be found.
