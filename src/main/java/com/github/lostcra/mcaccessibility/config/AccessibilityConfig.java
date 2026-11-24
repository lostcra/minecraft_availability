package com.github.lostcra.mcaccessibility.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccessibilityConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AccessibilityConfig instance;

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private boolean masterEnabled = true;
    private boolean chatNarrationEnabled = false;
    private boolean screenNarrationEnabled = true;
    private boolean inventoryNarrationEnabled = true;
    private boolean advancementNarrationEnabled = true;
    private boolean detailedTooltips = true;
    private boolean keyboardInventoryNav = true;
    private boolean keyboardAdvancementsNav = true;
    private boolean narrateHotkeys = true;
    private boolean f3NarrationEnabled = true;
    private int speechRate = 0; // -10 to 10
    private int speechVolume = 100; // 0 to 100
    private String ttsEngine = isWindows() ? "WINDOWS_SAPI" : "MOJANG"; // "WINDOWS_SAPI", "MOJANG" or "CUSTOM_..."
    private String selectedVoice = "Default";
    private String punctuationMode = "NONE"; // "NONE", "SOME", "ALL"
    private boolean readEmojis = true;
    private String capitalMode = "WORD"; // "OFF", "WORD", "PITCH", "BEEP"
    private boolean interruptOnKeyPress = true;
    private List<CustomSynthesizerConfig> customSynthesizers = new ArrayList<>();

    public static AccessibilityConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, AccessibilityConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance == null) {
            instance = new AccessibilityConfig();
            instance.save();
        }
        if (instance.customSynthesizers == null) {
            instance.customSynthesizers = new ArrayList<>();
        }
        if (instance.ttsEngine == null || instance.ttsEngine.isEmpty()) {
            instance.ttsEngine = isWindows() ? "WINDOWS_SAPI" : "MOJANG";
        }
        if (instance.selectedVoice == null || instance.selectedVoice.isEmpty()) {
            instance.selectedVoice = "Default";
        }
        if (instance.punctuationMode == null) {
            instance.punctuationMode = "NONE";
        }
        if (instance.capitalMode == null) {
            instance.capitalMode = "WORD";
        }
    }

    public void save() {
        File configFile = getConfigFile();
        configFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getConfigFile() {
        try {
            return FabricLoader.getInstance().getConfigDir().resolve("minecraft_availability.json").toFile();
        } catch (Throwable t) {
            return new File("config/minecraft_availability.json");
        }
    }

    public boolean isMasterEnabled() { return masterEnabled; }
    public void setMasterEnabled(boolean masterEnabled) { this.masterEnabled = masterEnabled; }

    public boolean isChatNarrationEnabled() { return chatNarrationEnabled; }
    public void setChatNarrationEnabled(boolean chatNarrationEnabled) { this.chatNarrationEnabled = chatNarrationEnabled; }

    public boolean isScreenNarrationEnabled() { return screenNarrationEnabled; }
    public void setScreenNarrationEnabled(boolean screenNarrationEnabled) { this.screenNarrationEnabled = screenNarrationEnabled; }

    public boolean isInventoryNarrationEnabled() { return inventoryNarrationEnabled; }
    public void setInventoryNarrationEnabled(boolean inventoryNarrationEnabled) { this.inventoryNarrationEnabled = inventoryNarrationEnabled; }

    public boolean isAdvancementNarrationEnabled() { return advancementNarrationEnabled; }
    public void setAdvancementNarrationEnabled(boolean advancementNarrationEnabled) { this.advancementNarrationEnabled = advancementNarrationEnabled; }

    public boolean isDetailedTooltips() { return detailedTooltips; }
    public void setDetailedTooltips(boolean detailedTooltips) { this.detailedTooltips = detailedTooltips; }

    public boolean isKeyboardInventoryNav() { return keyboardInventoryNav; }
    public void setKeyboardInventoryNav(boolean keyboardInventoryNav) { this.keyboardInventoryNav = keyboardInventoryNav; }

    public boolean isKeyboardAdvancementsNav() { return keyboardAdvancementsNav; }
    public void setKeyboardAdvancementsNav(boolean keyboardAdvancementsNav) { this.keyboardAdvancementsNav = keyboardAdvancementsNav; }

    public boolean isNarrateHotkeys() { return narrateHotkeys; }
    public void setNarrateHotkeys(boolean narrateHotkeys) { this.narrateHotkeys = narrateHotkeys; }

    public int getSpeechRate() { return speechRate; }
    public void setSpeechRate(int speechRate) { this.speechRate = Math.max(-10, Math.min(10, speechRate)); }

    public int getSpeechVolume() { return speechVolume; }
    public void setSpeechVolume(int speechVolume) { this.speechVolume = Math.max(0, Math.min(100, speechVolume)); }

    public String getTtsEngine() { return ttsEngine; }
    public void setTtsEngine(String ttsEngine) { this.ttsEngine = ttsEngine; }

    public String getSelectedVoice() { return selectedVoice; }
    public void setSelectedVoice(String selectedVoice) { this.selectedVoice = selectedVoice; }

    public String getPunctuationMode() { return punctuationMode != null ? punctuationMode : "NONE"; }
    public void setPunctuationMode(String punctuationMode) { this.punctuationMode = punctuationMode; }

    public boolean isReadEmojis() { return readEmojis; }
    public void setReadEmojis(boolean readEmojis) { this.readEmojis = readEmojis; }

    public String getCapitalMode() { return capitalMode != null ? capitalMode : "WORD"; }
    public void setCapitalMode(String capitalMode) { this.capitalMode = capitalMode; }

    public boolean isInterruptOnKeyPress() { return interruptOnKeyPress; }
    public void setInterruptOnKeyPress(boolean interruptOnKeyPress) { this.interruptOnKeyPress = interruptOnKeyPress; }

    public boolean isF3NarrationEnabled() { return f3NarrationEnabled; }
    public void setF3NarrationEnabled(boolean f3NarrationEnabled) { this.f3NarrationEnabled = f3NarrationEnabled; }

    public List<CustomSynthesizerConfig> getCustomSynthesizers() {
        if (customSynthesizers == null) {
            customSynthesizers = new ArrayList<>();
        }
        return customSynthesizers;
    }

    public void setCustomSynthesizers(List<CustomSynthesizerConfig> customSynthesizers) {
        this.customSynthesizers = customSynthesizers;
    }

    public void addCustomSynthesizer(CustomSynthesizerConfig cfg) {
        getCustomSynthesizers().removeIf(c -> c.getId().equalsIgnoreCase(cfg.getId()));
        getCustomSynthesizers().add(cfg);
        save();
    }

    public void removeCustomSynthesizer(String id) {
        getCustomSynthesizers().removeIf(c -> c.getId().equalsIgnoreCase(id));
        save();
    }
}
