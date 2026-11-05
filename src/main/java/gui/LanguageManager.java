package gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    
    public enum Language {
        HUNGARIAN("hu"),
        ENGLISH("en");
        
        private String code;
        Language(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    private static LanguageManager instance = null;
    private Language currentLanguage;
    private Map<String, Map<String, String>> translations;
    private PropertyChangeSupport pcs;
    
    private LanguageManager() {
        currentLanguage = Language.ENGLISH;
        translations = new HashMap<>();
        pcs = new PropertyChangeSupport(this);
        loadTranslations();
    }
    
    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }
    
    private void loadTranslations() {
        // Hungarian translations
        Map<String, String> hungarianMap = new HashMap<>();
        hungarianMap.put("app.title", "Áramkör szimulátor");
        hungarianMap.put("app.author", "Simon Zoltán (2025)");
        hungarianMap.put("file.open", "Fájl megnyitás...");
        hungarianMap.put("file.save", "Fájl mentés...");
        hungarianMap.put("file.all", "Az összes fájl");
        hungarianMap.put("file.text", "Szöveges fájlok");
        hungarianMap.put("sim.running", "Szimuláció folyamatban.");
        hungarianMap.put("sim.paused", "Szimuláció szüneteltetve.");
        hungarianMap.put("sim.stopped", "Szimuláció leállítva.");
        hungarianMap.put("connection.wrong", "Hibás kapcsolás!");
        hungarianMap.put("connection.correct", "Helyes kapcsolás.");
        hungarianMap.put("component.voltage", "Feszültségforrás");
        hungarianMap.put("component.sine", "Szinuszos feszültségforrás");
        hungarianMap.put("component.square", "Négyszög feszültségforrás");
        hungarianMap.put("component.triangle", "Háromszög feszültségforrás");
        hungarianMap.put("component.sawtooth", "Fűrészfog feszültségforrás");
        hungarianMap.put("component.resistance", "Ellenállás");
        hungarianMap.put("component.wire", "Vezeték");
        hungarianMap.put("component.capacitor", "Kondenzátor");
        hungarianMap.put("component.inductor", "Induktor");
        hungarianMap.put("component.voltmeter", "Analóg voltmérő");
        hungarianMap.put("component.ammeter", "Analóg ampermérő");
        hungarianMap.put("component.ground", "Földelés");
        hungarianMap.put("component.current", "Áramforrás");
        hungarianMap.put("component.bjt", "BJT Tranzisztor");
        hungarianMap.put("audio.voltage", "Feszültség");
        hungarianMap.put("audio.current", "Áram");
        hungarianMap.put("audio.input", "Potenciál (be)");
        hungarianMap.put("audio.output", "Potenciál (ki)");
        hungarianMap.put("menu.file", "Fájl");
        hungarianMap.put("menu.new", "Új");
        hungarianMap.put("menu.open", "Megnyitás");
        hungarianMap.put("menu.save", "Mentés");
        hungarianMap.put("menu.exit", "Kilépés");
        hungarianMap.put("menu.simulation", "Szimuláció");
        hungarianMap.put("menu.start", "Indítás");
        hungarianMap.put("menu.pause", "Szüneteltetés");
        hungarianMap.put("menu.stop", "Leállítás");
        hungarianMap.put("menu.help", "Súgó");
        hungarianMap.put("menu.about", "Névjegy");
        hungarianMap.put("menu.language", "Nyelv");
        hungarianMap.put("toolbar.volume", "Hangerő:");
        hungarianMap.put("properties.title", "Tulajdonságok");
        hungarianMap.put("scope.mode", "Mód");
        hungarianMap.put("scope.reset", "Visszaállítás");
        hungarianMap.put("scope.start", "Intervallum kezdete");
        hungarianMap.put("scope.length", "Intervallum hossza");
        
        translations.put(Language.HUNGARIAN.getCode(), hungarianMap);
        
        // English translations
        Map<String, String> englishMap = new HashMap<>();
        englishMap.put("app.title", "Electrical Network Simulator");
        englishMap.put("app.author", "Simon Zoltán (2025)");
        englishMap.put("file.open", "Open File...");
        englishMap.put("file.save", "Save File...");
        englishMap.put("file.all", "All files");
        englishMap.put("file.text", "Text files");
        englishMap.put("sim.running", "Simulation running.");
        englishMap.put("sim.paused", "Simulation paused.");
        englishMap.put("sim.stopped", "Simulation stopped.");
        englishMap.put("connection.wrong", "Incorrect connection!");
        englishMap.put("connection.correct", "Correct connection.");
        englishMap.put("component.voltage", "Voltage Source");
        englishMap.put("component.sine", "Sinusoidal Voltage Source");
        englishMap.put("component.square", "Square Voltage Source");
        englishMap.put("component.triangle", "Triangle Voltage Source");
        englishMap.put("component.sawtooth", "Sawtooth Voltage Source");
        englishMap.put("component.resistance", "Resistance");
        englishMap.put("component.wire", "Wire");
        englishMap.put("component.capacitor", "Capacitor");
        englishMap.put("component.inductor", "Inductor");
        englishMap.put("component.voltmeter", "Analog Voltmeter");
        englishMap.put("component.ammeter", "Analog Ammeter");
        englishMap.put("component.ground", "Ground");
        englishMap.put("component.current", "Current Source");
        englishMap.put("component.bjt", "BJT Transistor");
        englishMap.put("audio.voltage", "Voltage");
        englishMap.put("audio.current", "Current");
        englishMap.put("audio.input", "Potential (In)");
        englishMap.put("audio.output", "Potential (Out)");
        englishMap.put("menu.file", "File");
        englishMap.put("menu.new", "New");
        englishMap.put("menu.open", "Open");
        englishMap.put("menu.save", "Save");
        englishMap.put("menu.exit", "Exit");
        englishMap.put("menu.simulation", "Simulation");
        englishMap.put("menu.start", "Start");
        englishMap.put("menu.pause", "Pause");
        englishMap.put("menu.stop", "Stop");
        englishMap.put("menu.help", "Help");
        englishMap.put("menu.about", "About");
        englishMap.put("menu.language", "Language");
        englishMap.put("toolbar.volume", "Volume:");
        englishMap.put("properties.title", "Properties");
        englishMap.put("scope.mode", "Mode");
        englishMap.put("scope.reset", "Reset");
        englishMap.put("scope.start", "Interval start");
        englishMap.put("scope.length", "Interval length");
        
        translations.put(Language.ENGLISH.getCode(), englishMap);
    }
    
    public String get(String key) {
        Map<String, String> languageMap = translations.get(currentLanguage.getCode());
        return languageMap.getOrDefault(key, key);
    }
    
    public void setLanguage(Language language) {
        if (currentLanguage != language) {
            Language oldLanguage = currentLanguage;
            currentLanguage = language;
            pcs.firePropertyChange("language", oldLanguage, language);
        }
    }
    
    public Language getCurrentLanguage() {
        return currentLanguage;
    }
    
    public void addLanguageListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener("language", listener);
    }
    
    public void removeLanguageListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener("language", listener);
    }
}
