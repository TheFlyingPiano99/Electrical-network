# VSCode Maven Configuration Guide

## ✅ Maven Integration Complete

Your VSCode is now configured to run Maven commands easily through multiple methods.

## 🚀 Quick Start Methods

### Method 1: Keyboard Shortcuts (Fastest)
| Shortcut | Action |
|----------|--------|
| `Ctrl+Shift+B` | Build (compile) |
| `Ctrl+Shift+R` | Run Application |
| `Ctrl+Shift+T` | Run Tests |
| `Ctrl+Shift+P` (then type task) | Show all Maven tasks |

### Method 2: Tasks Command Palette
1. Press `Ctrl+Shift+P`
2. Type: `Tasks: Run Task`
3. Select a Maven task from the list

### Method 3: Run Menu
1. Go to **Terminal → Run Task**
2. Select the Maven task you want

### Method 4: Terminal
```bash
# Open integrated terminal: Ctrl+`
mvn clean javafx:run
```

## 📋 Available Maven Tasks

All tasks configured:
- ✅ **Maven: Build (compile)** - Compile source code
- ✅ **Maven: Run Application** - Build and run the app
- ✅ **Maven: Package JAR** - Create JAR file
- ✅ **Maven: Run Tests** - Execute unit tests
- ✅ **Maven: Install** - Install to local Maven repository
- ✅ **Maven: Clean** - Clean build artifacts
- ✅ **Maven: Compile Only** - Quick compile without clean

## 🔧 VSCode Configuration Files

### `.vscode/settings.json`
- Configures Maven executable preferences
- Sets up favorite Maven commands
- Enables Java code formatting

### `.vscode/tasks.json`
- Defines 7 Maven tasks
- Sets up problem matchers for error detection
- Configures terminal behavior

### `.vscode/keybindings.json`
- Custom keyboard shortcuts for Maven tasks
- Overrides default VSCode bindings for faster access

### `.vscode/launch.json`
- Java debugging configuration
- Allows F5 to debug the application

## 💡 Recommended Workflows

### Daily Development
1. **Make changes** to Java files
2. **Ctrl+Shift+B** to compile immediately
3. **Ctrl+Shift+R** to run and test

### Before Committing
1. **Ctrl+Shift+T** to run all tests
2. **Fix any failing tests**
3. **Ctrl+Shift+B** for final build

### Preparing Release
1. **Ctrl+Shift+T** to run tests
2. **Ctrl+Shift+P** (then "Package JAR") to build JAR
3. JAR is in: `target/ElectricalNetwork-1.0-SNAPSHOT.jar`

## 📊 Task Output

When you run a task:
- Terminal opens automatically in integrated panel
- Output shows in real-time
- Build errors highlighted in red
- Problem panel shows issues with line numbers
- You can click errors to jump to source code

## 🔍 Troubleshooting Maven

### Maven command not found
```bash
# Check Maven installation
mvn --version

# If not installed:
# Download from https://maven.apache.org/download.cgi
# Add to PATH environment variable
# Restart VSCode
```

### Build fails with "Cannot find JAR"
```bash
# Run this to download dependencies
mvn clean install

# This downloads all required libraries
```

### Tests fail to run
```bash
# Ensure tests compile
mvn test-compile

# Then run
mvn test
```

### Slow first build
First build downloads all Maven dependencies (~500MB). Subsequent builds are much faster.

## 🎯 Tips & Tricks

### Auto-compile on save
Currently tasks run manually. To auto-compile:
1. Open integrated terminal: `Ctrl+``
2. Run: `mvn compile -watch`
3. This watches files and compiles automatically

### Quick Maven commands in terminal
```bash
# Just type these directly in integrated terminal (Ctrl+`)
mvn clean                      # Clean build artifacts
mvn compile                    # Compile only
mvn compile javafx:run         # Run without clean
mvn package -DskipTests        # Package without tests
mvn dependency:tree            # Show dependency tree
mvn help:describe -Dplugin=org.apache.maven.plugins:maven-compiler-plugin
```

### Problem Matcher
- Automatically detects compilation errors
- Errors appear in Problems panel
- Click to jump to source file
- Shows line and column numbers

## 🔄 Maven Lifecycle

Understanding Maven phases:
```
validate → compile → test → package → verify → install → deploy
                ↑         ↑         ↑
            Run on      Run on    Run on
          Ctrl+Shift+B Ctrl+Shift+T Ctrl+Shift+P
```

When you run "Maven: Run Application":
1. ✅ Cleans previous build
2. ✅ Compiles Java source
3. ✅ Packages resources
4. ✅ Runs with JavaFX plugin

## 🌐 Java Extensions Integration

VSCode extensions work with Maven:
- **Extension Pack for Java** - Full Java support
- **Maven for Java** - Maven integration
- **Project Manager for Java** - Project navigation
- **Debugger for Java** - Debug with F5

All these integrate with the Maven tasks above.

## 📝 Custom Maven Profiles

To add custom builds, edit `.vscode/tasks.json` and add:

```json
{
    "label": "Maven: Custom Command",
    "type": "shell",
    "command": "mvn",
    "args": [
        "your-custom-phase",
        "-Dyour-property=value"
    ],
    "group": {
        "kind": "build",
        "isDefault": false
    },
    "presentation": {
        "reveal": "always",
        "panel": "new"
    }
}
```

Then run with: `Ctrl+Shift+P` → "Tasks: Run Task" → "Maven: Custom Command"

## 🎓 Learning Resources

- [Maven Official Docs](https://maven.apache.org/guides/)
- [Maven Plugin Guide](https://maven.apache.org/plugins/)
- [JavaFX Maven Plugin](https://github.com/openjfx/javafx-maven-plugin)

## ✨ Summary

You now have:
- ✅ 7 preconfigured Maven tasks
- ✅ Keyboard shortcuts for common operations
- ✅ Integrated terminal output
- ✅ Error detection and highlighting
- ✅ Quick access from Command Palette
- ✅ Professional Maven workflow

**Start building!** Use `Ctrl+Shift+B` to compile and `Ctrl+Shift+R` to run.
