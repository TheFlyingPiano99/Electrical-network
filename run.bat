@echo off
REM Run the Electrical Network Simulator application
REM This script properly sets up JavaFX module path for Windows

setlocal enabledelayedexpansion

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Navigate to the target directory where the JAR is located
cd /d "%SCRIPT_DIR%target"

REM Check if the JAR exists
if not exist "ElectricalNetwork.jar" (
    echo Error: ElectricalNetwork.jar not found in target directory
    echo Please run: mvn clean package
    pause
    exit /b 1
)

REM Run the application with JavaFX module path
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar ElectricalNetwork.jar

endlocal
