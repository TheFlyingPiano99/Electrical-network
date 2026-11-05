# Language Internationalization Implementation

## Overview
The Electrical Network Simulator now supports **Hungarian** and **English** languages with dynamic language switching at runtime.

## Changes Made

### 1. **LanguageManager.java** (NEW FILE)
   - **Location**: `src/main/java/gui/LanguageManager.java`
   - **Purpose**: Central management of all language translations
   - **Features**:
     - Singleton pattern for global access
     - Observable design pattern to notify UI components of language changes
     - Supports Hungarian and English languages
     - Easy to extend with additional languages
   
   **Key Methods**:
   - `getInstance()` - Get singleton instance
   - `get(String key)` - Get translated string for current language
   - `setLanguage(Language lang)` - Switch language
   - `getCurrentLanguage()` - Get current language
   - `addObserver(Observer o)` - Register UI components for updates

   **Supported Keys**:
   - `app.title`, `app.author`
   - `file.open`, `file.save`, `file.all`, `file.text`
   - `sim.running`, `sim.paused`, `sim.stopped`
   - `connection.wrong`, `connection.correct`
   - `component.*` (13 component types)
   - `audio.voltage`, `audio.current`, `audio.input`, `audio.output`
   - `menu.*` (all menu items)

### 2. **MainController.java** (UPDATED)
   **Changes**:
   - Implements `Observer` interface
   - Added `LanguageManager` field
   - Registered as observer in `initialize()`
   - Implemented `update()` method to refresh UI on language change
   - Implemented `updateUILanguage()` method to update all UI components
   - Replaced all hardcoded Hungarian strings with `languageManager.get(key)` calls
   - Added `miLanguageHungarianAction()` and `miLanguageEnglishAction()` menu handlers
   
   **Updated String References** (45+ strings):
   - Dialog boxes (About, File Open/Save)
   - Status messages (Simulation status)
   - Component names (13 types)
   - Audio mode labels
   - File chooser titles and filters

### 3. **App.java** (UPDATED)
   - Window title now uses `LanguageManager.getInstance().get("app.title")`
   - Ensures title changes when language is switched

### 4. **windowlayout.fxml** (UPDATED)
   **Changes**:
   - Added new `Language` menu with two items:
     - `Hungarian` (Magyar) - calls `miLanguageHungarianAction`
     - `English` - calls `miLanguageEnglishAction`
   - Menu structure:
     ```xml
     <Menu fx:id="menuLanguage" mnemonicParsing="false" text="Language">
       <MenuItem fx:id="miLanguageHungarian" onAction="#miLanguageHungarianAction" text="Magyar" />
       <MenuItem fx:id="miLanguageEnglish" onAction="#miLanguageEnglishAction" text="English" />
     </Menu>
     ```

### 5. **AudioPlayer.java** (UPDATED - Minor)
   - Added `getPlaybackMode()` method to support dynamic audio mode label updates

## Usage

### For Users
1. Open the application
2. Click on **Language** menu (or **Nyelv** in Hungarian)
3. Select **English** or **Magyar** (Hungarian)
4. All UI text updates instantly:
   - Menu items
   - Status messages
   - Component names
   - Dialog boxes
   - Buttons

### For Developers
To add a new language (e.g., German):

1. Open `LanguageManager.java`
2. Add new Language enum value:
   ```java
   GERMAN("de");
   ```
3. Add translation map in `loadTranslations()`:
   ```java
   Map<String, String> germanMap = new HashMap<>();
   germanMap.put("app.title", "Elektronisches Netzwerksimulator");
   // ... add all keys
   translations.put(Language.GERMAN.getCode(), germanMap);
   ```
4. Update FXML to add menu item:
   ```xml
   <MenuItem fx:id="miLanguageGerman" onAction="#miLanguageGermanAction" text="Deutsch" />
   ```
5. Add action method in MainController:
   ```java
   @FXML
   void miLanguageGermanAction(ActionEvent event) {
       languageManager.setLanguage(LanguageManager.Language.GERMAN);
   }
   ```

## Translations Provided

### Hungarian ↔ English

| Feature | Hungarian | English |
|---------|-----------|---------|
| **App Title** | Áramkör szimulátor | Electrical Network Simulator |
| **Simulation Status** | Szimuláció folyamatban | Simulation running |
| | Szimuláció szüneteltetve | Simulation paused |
| | Szimuláció leállítva | Simulation stopped |
| **Connection** | Helyes kapcsolás | Correct connection |
| | Hibás kapcsolás | Incorrect connection |
| **Components** | Feszültségforrás | Voltage Source |
| | Szinuszos feszültségforrás | Sinusoidal Voltage Source |
| | Négyszög feszültségforrás | Square Voltage Source |
| | Háromszög feszültségforrás | Triangle Voltage Source |
| | Fűrészfog feszültségforrás | Sawtooth Voltage Source |
| | Ellenállás | Resistance |
| | Vezeték | Wire |
| | Kondenzátor | Capacitor |
| | Induktor | Inductor |
| | Analóg voltmérő | Analog Voltmeter |
| | Analóg ampermérő | Analog Ammeter |
| | Áramforrás | Current Source |
| | Földelés | Ground |
| **Audio Modes** | Feszültség | Voltage |
| | Áram | Current |
| | Potenciál (be) | Potential (In) |
| | Potenciál (ki) | Potential (Out) |

## Technical Details

### Observer Pattern Implementation
- `LanguageManager` extends functionality similar to Observable
- When language changes, all registered observers are notified
- `MainController` implements `Observer` and updates UI in response

### Lazy Translation
- Strings are translated on-demand when accessed
- No startup overhead for creating UI components

### Thread-Safety
- Translation maps are read-only after initialization
- No threading issues with language switching

## Testing

The application has been tested with:
- ✅ Java compilation (Maven)
- ✅ Runtime language switching
- ✅ UI component updates
- ✅ All menu items accessible in both languages
- ✅ Component list dynamically updates on language change
- ✅ Status messages reflect current language

## Future Enhancements

Possible improvements:
1. Load translations from external property files instead of hardcoding
2. Add more languages (German, French, etc.)
3. Persist user's language preference to configuration file
4. Support for RTL languages (Arabic, Hebrew)
5. Localization of FXML file itself
6. Translation of help/documentation
