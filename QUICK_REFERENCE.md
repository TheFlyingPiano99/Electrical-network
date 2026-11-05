# Quick Reference - VSCode Maven Setup

## 🎯 Most Common Commands

```
Ctrl+Shift+B  →  Build (compile)
Ctrl+Shift+R  →  Run Application  
Ctrl+Shift+T  →  Run Tests
```

## 📋 All Maven Tasks

Press `Ctrl+Shift+P` then type `Tasks: Run Task` and select:

| Task Name | What it does |
|-----------|-------------|
| **Maven: Build (compile)** | Compile Java source code |
| **Maven: Run Application** | Build & launch the app |
| **Maven: Package JAR** | Create JAR file for distribution |
| **Maven: Run Tests** | Execute all unit tests |
| **Maven: Install** | Install to Maven repository |
| **Maven: Clean** | Delete build artifacts |
| **Maven: Compile Only** | Quick compile (no clean) |

## 🔄 Typical Workflow

```
1. Write/modify code
2. Ctrl+Shift+B          (compile to check for errors)
3. Ctrl+Shift+R          (run app to test)
4. Ctrl+Shift+T          (run tests before commit)
5. Commit to git
```

## 🐛 Debug with Breakpoints

```
1. Click left margin in editor to set breakpoint (red dot)
2. Press F5 to start debugging
3. App launches and stops at breakpoint
4. Use Debug panel to:
   - Step over (F10)
   - Step into (F11)
   - Continue (F5)
   - View variables
```

## 📦 Build Release JAR

```
1. Ctrl+Shift+T          (run all tests)
2. Fix any failing tests
3. Ctrl+Shift+P          (package JAR)
4. JAR saved to: target/ElectricalNetwork-1.0-SNAPSHOT.jar
```

## 🌍 Switch Language

In running app:
- Click **Language** menu → **Magyar** (Hungarian)
- Click **Language** menu → **English**

## 🆘 If Something Breaks

```
1. Try: Ctrl+Shift+P → "Java: Clean Java Language Server Workspace"
2. Or:  Ctrl+Shift+P → "View: Toggle Problem"
3. Or:  Terminal → Run Task → Maven: Clean
4. Then: Ctrl+Shift+B to rebuild
```

## 📊 Files Configuration

```
.vscode/
├── launch.json          (F5 debug config)
├── tasks.json          (Maven tasks)
├── settings.json       (VSCode settings)
└── keybindings.json    (Keyboard shortcuts)
```

## 🚀 Remember

- **Blue → Running/Debugging**
- **Red → Errors**
- **Yellow → Warnings**
- **Green → Build Success**

## 💡 Pro Tips

- Press `Ctrl+`` to open integrated terminal
- Click error in Problems panel to jump to line
- Right-click in editor → "Format Document" (Ctrl+Shift+F)
- Ctrl+Shift+P for everything in VSCode

---

**For more details:** See `MAVEN_SETUP.md` or `VSCODE_SETUP.md`
