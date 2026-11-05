# Running the Electrical Network Simulator in VS Code

## Quick Start

### Method 1: Using VS Code Debug (Recommended)
1. Open the project in VS Code
2. Press `F5` or go to **Run → Start Debugging**
3. Select **"ElectricalNetwork App"** from the launch configuration
4. The application will build and launch

### Method 2: Using VS Code Tasks
1. Open Command Palette: `Ctrl+Shift+P` (Windows)
2. Type **"Run Task"**
3. Select **"Run Application"**
4. The application will build and launch using Maven

### Method 3: Using Terminal Directly
```bash
# Build the project
mvn clean compile

# Run the application
mvn clean javafx:run
```

## Available Tasks

The project includes several pre-configured tasks in `.vscode/tasks.json`:

| Task | Command | Purpose |
|------|---------|---------|
| **Build Project** | `mvn clean compile` | Compile source files |
| **Run Application** | `mvn clean javafx:run` | Build and run the app with JavaFX support |
| **Package Application** | `mvn clean package -DskipTests` | Create JAR file |
| **Run Tests** | `mvn test` | Run unit tests |

### To Run a Task:
1. Press `Ctrl+Shift+P`
2. Type **"Tasks: Run Task"**
3. Select the desired task
4. The task will execute in the integrated terminal

## VS Code Configuration Files

### `.vscode/launch.json`
- Configures Java debugging
- Sets main class to `gui.App`
- Uses project root as working directory

### `.vscode/tasks.json`
- Defines Maven build tasks
- Configured for easy access from Command Palette
- Supports building, running, packaging, and testing

## Troubleshooting

### "Project not detected" error
- Ensure you have Maven installed and it's in your PATH
- Run `mvn --version` to verify
- Restart VS Code if Maven was recently added to PATH

### "Cannot find or load main class gui.App"
- Ensure the project compiles: `mvn clean compile`
- Check that `.classpath` points to `src/main/java`
- Run `java: Clean Java Language Server Workspace` from Command Palette

### Red squiggles in editor
- These are mostly in test files due to type mismatches (already fixed)
- Main application code should have no squiggles
- If you see squiggles, try: `Ctrl+Shift+P` → **"Java: Clean Java Language Server Workspace"**

### Application window doesn't appear
- Check the integrated terminal for error messages
- Ensure you have JavaFX 23 dependencies installed
- Try running: `mvn clean javafx:run`

## Language Support

The application supports **Hungarian** and **English** languages:

1. Run the application
2. Go to **Language** menu (top menu bar)
3. Select **"Magyar"** (Hungarian) or **"English"**
4. UI text updates instantly

## Key Features Implemented

✅ Full Java project with Maven build system
✅ JavaFX GUI with multiple canvases
✅ Language internationalization (Hungarian/English)
✅ Unit tests with JUnit 5
✅ VS Code integrated debugging and tasks
✅ Complex number mathematics
✅ Electrical network simulation

## VS Code Extensions Recommended

For best experience, install:
- **Extension Pack for Java** (Microsoft)
- **Maven for Java** (Microsoft)
- **Debugger for Java** (Microsoft)

## Building a JAR File

To create a standalone JAR:
```bash
mvn clean package -DskipTests
```

This creates: `target/ElectricalNetwork-1.0-SNAPSHOT.jar`

## Notes

- The application requires Java 23+
- JavaFX 23 is configured as a Maven dependency
- Working directory for the app is the project root (important for finding `windowlayout.fxml`)
- Build artifacts are in the `target/` directory
