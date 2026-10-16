@echo off
setlocal
call build.bat
if errorlevel 1 exit /b 1
if not exist "dist" mkdir dist
if exist "package-input" rmdir /s /q "package-input"
mkdir package-input
copy "lib\mysql-connector-j.jar" "package-input\mysql-connector-j.jar" >nul
(
  echo Main-Class: ExpenseTracker
  echo Class-Path: mysql-connector-j.jar
  echo.
) > package-input\manifest.mf
jar cfm package-input\ExpenseTracker.jar package-input\manifest.mf -C out .
if errorlevel 1 exit /b 1
jpackage --type app-image --name ExpenseTrackerApp --input package-input --main-jar ExpenseTracker.jar --main-class ExpenseTracker --dest dist
if errorlevel 1 (
  echo jpackage failed. Check that JDK 21 and jpackage are installed.
  exit /b 1
)
echo App image created under dist\ExpenseTrackerApp
