package com.github.lostcra.mcaccessibility.config;

import com.github.lostcra.mcaccessibility.tts.CustomCommandBackend;
import com.github.lostcra.mcaccessibility.tts.SpeechBackend;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.List;

public class AdvancedSettingsScreen extends Screen {
    private final Screen parent;
    private SpeechBackend currentBackend;

    public AdvancedSettingsScreen(Screen parent) {
        super(Component.translatable("mcaccessibility.advanced.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AccessibilityConfig config = AccessibilityConfig.get();
        this.currentBackend = SpeechEngine.getInstance().getActiveBackend(config);

        int cx = this.width / 2;
        int y = 28;

        // Title
        addRenderableOnly(new StringWidget(cx - 155, 8, 310, 14,
                Component.translatable("mcaccessibility.advanced.title"), this.font));

        // 1. Punctuation Mode (150) & Capital Letters Mode (150)
        CycleButton<String> punctBtn = addRenderableWidget(CycleButton.builder(
                mode -> {
                    switch (mode) {
                        case "NONE": return Component.translatable("mcaccessibility.advanced.punct.none");
                        case "SOME": return Component.translatable("mcaccessibility.advanced.punct.some");
                        case "ALL": return Component.translatable("mcaccessibility.advanced.punct.all");
                        default: return Component.literal(mode);
                    }
                },
                config.getPunctuationMode()
        ).withValues(List.of("NONE", "SOME", "ALL")).create(
                cx - 155, y, 150, 20,
                Component.translatable("mcaccessibility.advanced.punct"),
                (btn, val) -> {
                    config.setPunctuationMode(val);
                    config.save();
                    SpeechEngine.getInstance().speak(Component.translatable("mcaccessibility.advanced.punct").getString() + ": " + btn.getMessage().getString(), true);
                }
        ));
        punctBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.punct_tooltip")));

        CycleButton<String> capitalBtn = addRenderableWidget(CycleButton.builder(
                mode -> {
                    switch (mode) {
                        case "OFF": return Component.translatable("mcaccessibility.advanced.capitals.off");
                        case "WORD": return Component.translatable("mcaccessibility.advanced.capitals.word");
                        case "PITCH": return Component.translatable("mcaccessibility.advanced.capitals.pitch");
                        case "BEEP": return Component.translatable("mcaccessibility.advanced.capitals.beep");
                        default: return Component.literal(mode);
                    }
                },
                config.getCapitalMode()
        ).withValues(List.of("OFF", "WORD", "PITCH", "BEEP")).create(
                cx + 5, y, 150, 20,
                Component.translatable("mcaccessibility.advanced.capitals"),
                (btn, val) -> {
                    config.setCapitalMode(val);
                    config.save();
                    SpeechEngine.getInstance().speak(Component.translatable("mcaccessibility.advanced.capitals").getString() + ": " + btn.getMessage().getString(), true);
                }
        ));
        capitalBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.capitals_tooltip")));
        y += 22;

        // 2. Emoji Reading (150) & Detailed Tooltips (150)
        CycleButton<Boolean> emojiBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isReadEmojis()).create(
                cx - 155, y, 150, 20,
                Component.translatable("mcaccessibility.advanced.emojis"),
                (btn, val) -> {
                    config.setReadEmojis(val);
                    config.save();
                    SpeechEngine.getInstance().speak(val ? "Чтение смайликов включено" : "Чтение смайликов отключено", true);
                }
        ));
        emojiBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.emojis_tooltip")));

        CycleButton<Boolean> tooltipsBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isDetailedTooltips()).create(
                cx + 5, y, 150, 20,
                Component.translatable("mcaccessibility.advanced.tooltips"),
                (btn, val) -> {
                    config.setDetailedTooltips(val);
                    config.save();
                }
        ));
        tooltipsBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.tooltips_tooltip")));
        y += 22;

        // 3. Narrate Hotkeys (150) & Keyboard Inventory Nav (150)
        CycleButton<Boolean> hotkeysBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isNarrateHotkeys()).create(
                cx - 155, y, 150, 20,
                Component.translatable("mcaccessibility.advanced.hotkeys"),
                (btn, val) -> {
                    config.setNarrateHotkeys(val);
                    config.save();
                }
        ));
        hotkeysBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.hotkeys_tooltip")));

        CycleButton<Boolean> invKeysBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isKeyboardInventoryNav()).create(
                cx + 5, y, 150, 20,
                Component.translatable("mcaccessibility.advanced.inv_keys"),
                (btn, val) -> {
                    config.setKeyboardInventoryNav(val);
                    config.save();
                }
        ));
        invKeysBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.inv_keys_tooltip")));
        y += 26;

        // 4. Synthesizer Specific Configuration Block
        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14,
                Component.translatable("mcaccessibility.advanced.synth_header"), this.font));
        y += 16;

        if (currentBackend instanceof CustomCommandBackend customBackend) {
            CustomSynthesizerConfig synthConfig = customBackend.getConfig();

            Button editSynthBtn = addRenderableWidget(Button.builder(
                    Component.translatable("mcaccessibility.advanced.configure_synth"),
                    btn -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreenAndShow(new CustomSynthesizerEditScreen(this, synthConfig));
                        }
                    }
            ).bounds(cx - 155, y, 150, 20).build());
            editSynthBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.configure_synth_tooltip")));

            Button openFolderBtn = addRenderableWidget(Button.builder(
                    Component.translatable("mcaccessibility.advanced.open_synth_folder"),
                    btn -> openFolderForFile(synthConfig.getExecutablePath())
            ).bounds(cx + 5, y, 150, 20).build());
            openFolderBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.open_synth_folder_tooltip")));
        } else if ("WINDOWS_SAPI".equalsIgnoreCase(currentBackend.getId())) {
            Button winSpeechBtn = addRenderableWidget(Button.builder(
                    Component.translatable("mcaccessibility.advanced.windows_speech"),
                    btn -> openWindowsSpeechSettings()
            ).bounds(cx - 155, y, 310, 20).build());
            winSpeechBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.windows_speech_tooltip")));
        } else {
            addRenderableOnly(new StringWidget(cx - 155, y, 310, 14,
                    Component.translatable("mcaccessibility.advanced.mojang_narrator_info"), this.font));
        }

        // Bottom Row: Back Button
        int bottomY = this.height - 26;
        Button backBtn = addRenderableWidget(Button.builder(
                CommonComponents.GUI_BACK,
                btn -> onClose()
        ).bounds(cx - 100, bottomY, 200, 20).build());
        backBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.advanced.back_tooltip")));
    }

    private void openFolderForFile(String path) {
        if (path == null || path.trim().isEmpty()) return;
        try {
            File f = new File(path);
            if (f.exists()) {
                File folder = f.isDirectory() ? f : f.getParentFile();
                String os = System.getProperty("os.name", "").toLowerCase();
                if (os.contains("win")) {
                    new ProcessBuilder("explorer.exe", "/select,", f.getAbsolutePath()).start();
                    SpeechEngine.getInstance().speak("Папка программы открыта в Проводнике Windows", true);
                    return;
                } else if (os.contains("mac")) {
                    new ProcessBuilder("open", "-R", f.getAbsolutePath()).start();
                    SpeechEngine.getInstance().speak("Папка программы открыта в Finder", true);
                    return;
                } else {
                    new ProcessBuilder("xdg-open", folder != null ? folder.getAbsolutePath() : f.getAbsolutePath()).start();
                    SpeechEngine.getInstance().speak("Папка программы открыта в файловом менеджере", true);
                    return;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        SpeechEngine.getInstance().speak("Файл программы не найден", true);
    }

    private void openWindowsSpeechSettings() {
        try {
            new ProcessBuilder("cmd.exe", "/c", "start ms-settings:speech").start();
            SpeechEngine.getInstance().speak("Открыты параметры распознавания и синтеза речи Windows", true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onClose() {
        AccessibilityConfig.get().save();
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }
}
