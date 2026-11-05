@echo off
set "APP=%~dp0target\ElectricalNetwork\bin\ElectricalNetwork.bat"

if not exist "%APP%" (
    echo Error: bundled application not found at "%APP%"
    echo Run "mvn clean package" from the project root first.
    pause
    exit /b 1
)

start "" "%APP%" %*
