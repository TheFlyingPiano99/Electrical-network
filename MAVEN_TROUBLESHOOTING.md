# Maven Troubleshooting Guide

## Common Issues & Solutions

### 1. "mvn: command not found" or "Maven not recognized"

**Problem:** Maven executable not in system PATH

**Solution:**
```powershell
# Check if Maven is installed
mvn --version

# If not installed, download from:
# https://maven.apache.org/download.cgi

# Add to environment variables:
# 1. Download Maven (e.g., apache-maven-3.9.x)
# 2. Extract to: C:\Program Files\maven
# 3. Add to PATH: C:\Program Files\maven\bin
# 4. Restart VSCode
# 5. Run: mvn --version

# Or set MAVEN_HOME environment variable:
# MAVEN_HOME = C:\Program Files\maven
# PATH = %MAVEN_HOME%\bin;%PATH%
```

### 2. Build fails: "Cannot find artifact"

**Problem:** Missing Maven dependencies

**Solution:**
```bash
# Download all dependencies
mvn clean install

# This can take a few minutes on first run
# Dependencies are cached in ~/.m2/repository
```

### 3. Build fails: "Could not find or load main class"

**Problem:** Classes not compiled or classpath issues

**Solution:**
```bash
# Clean and recompile
mvn clean compile

# Clear Java Language Server cache
# In VSCode: Ctrl+Shift+P → "Java: Clean Java Language Server Workspace"

# Rebuild
mvn clean compile
```

### 4. Tests fail to compile/run

**Problem:** Test dependencies or type issues

**Solution:**
```bash
# Verify tests compile
mvn test-compile

# Run tests with verbose output
mvn test -X

# Run specific test
mvn test -Dtest=MyMathTest
```

### 5. Application runs but window doesn't appear

**Problem:** JavaFX runtime issues or working directory

**Solution:**
```bash
# Run with explicit JavaFX modules
mvn clean javafx:run

# Check working directory is project root
# Ensure windowlayout.fxml is in project root

# Run from correct directory
cd E:\coding\Electrical-network
mvn javafx:run
```

### 6. JAR created but won't run

**Problem:** Missing dependencies or wrong main class

**Solution:**
```bash
# Check manifest
jar tf target/ElectricalNetwork-1.0-SNAPSHOT.jar | grep MANIFEST

# Rebuild JAR
mvn clean package -DskipTests

# Try running with classpath
java -jar target/ElectricalNetwork-1.0-SNAPSHOT.jar

# If it fails, use maven plugin
mvn javafx:run
```

### 7. Build slow or hangs

**Problem:** Network issues, large downloads, or Java heap

**Solution:**
```bash
# Check Maven version
mvn --version

# Increase heap size
set MAVEN_OPTS=-Xmx1024m
mvn clean compile

# Skip tests to speed up build
mvn clean install -DskipTests

# Use offline mode if dependencies cached
mvn clean compile -o

# Remove ~/.m2/repository and redownload
# (Be careful - this is large!)
```

### 8. Keyboard shortcut not working

**Problem:** Conflict with another VSCode extension or keybinding

**Solution:**
```
1. Ctrl+Shift+P → "Preferences: Open Keyboard Shortcuts"
2. Search for the command (e.g., "Maven")
3. Check for conflicts (red ! icon)
4. Remove conflicting binding
5. Or modify .vscode/keybindings.json

Alternative: Use Ctrl+Shift+P → "Tasks: Run Task" instead
```

### 9. VSCode says "Project not detected"

**Problem:** Not in correct project directory

**Solution:**
```powershell
# Ensure pom.xml is in current directory
dir pom.xml

# Or open VSCode from project root
cd E:\coding\Electrical-network
code .

# Verify .vscode folder exists
dir .vscode
```

### 10. Build succeeds but VSCode shows errors

**Problem:** Java Language Server cache out of sync

**Solution:**
```
1. Ctrl+Shift+P → "Java: Clean Java Language Server Workspace"
2. Wait for reindex (1-2 minutes)
3. Verify build: Ctrl+Shift+B
4. Restart VSCode if still issues
```

## 🔍 Debugging Build Issues

### Get detailed build output
```bash
mvn clean compile -X
```

### Check Maven configuration
```bash
mvn help:describe -Dplugin=org.apache.maven.plugins:maven-compiler-plugin

# Or for JavaFX plugin
mvn help:describe -Dplugin=org.openjfx:javafx-maven-plugin
```

### View dependency tree
```bash
mvn dependency:tree

# With detailed output
mvn dependency:tree -Dverbose
```

### Verify POM file
```bash
mvn validate
```

## 📊 Common Error Messages

| Error | Cause | Fix |
|-------|-------|-----|
| `Cannot find symbol` | Import missing | Run `mvn clean compile` |
| `[ERROR] COMPILATION ERROR` | Syntax error | Fix Java syntax, recompile |
| `Cannot resolve plugin` | Maven plugin not installed | Run `mvn clean install` |
| `Failed to execute goal` | Plugin configuration issue | Check pom.xml plugin config |
| `No goals have been specified` | Maven command malformed | Check task.json args array |
| `Java version mismatch` | Wrong Java/compiler version | Set `<source>23</source>` in pom.xml |
| `JavaFX runtime error` | JavaFX modules not loaded | Use `mvn clean javafx:run` |

## 🆘 Emergency Fixes

If everything is broken:

```bash
# Nuclear option - clean everything
mvn clean
rm -r .m2/repository  (or del %USERPROFILE%\.m2\repository on Windows)
mvn clean install

# Rebuild VSCode Java Language Server
# Ctrl+Shift+P → "Java: Clean Java Language Server Workspace"

# Restart VSCode
# Ctrl+K → Ctrl+W (close all) or just restart
```

## 📞 Getting Help

1. **Check build output:** `Ctrl+Shift+P` → "View: Toggle Problem"
2. **Read error messages carefully** - they usually tell you what's wrong
3. **Search error in browser** - most issues have solutions online
4. **Check Maven docs:** https://maven.apache.org/
5. **Check JavaFX Maven Plugin:** https://github.com/openjfx/javafx-maven-plugin

## ✅ Verify Setup Works

```bash
# Run this test sequence
mvn --version                    # Check Maven installed
mvn clean                        # Clean build
mvn compile                      # Compile code
mvn test                         # Run tests
mvn package -DskipTests         # Build JAR
mvn clean javafx:run            # Run application
```

If all 6 commands succeed, your Maven setup is perfect!

---

**Need more help?** Check MAVEN_SETUP.md or VSCODE_SETUP.md
