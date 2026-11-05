#!/bin/bash
# Run the Electrical Network Simulator application
# This script properly sets up JavaFX module path for Linux/Mac

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Navigate to the target directory where the JAR is located
cd "$SCRIPT_DIR/target"

# Check if the JAR exists
if [ ! -f "ElectricalNetwork.jar" ]; then
    echo "Error: ElectricalNetwork.jar not found in target directory"
    echo "Please run: mvn clean package"
    exit 1
fi

# Run the application with JavaFX module path
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar ElectricalNetwork.jar
