package com.github.lostcra.mcaccessibility.config;

import com.github.lostcra.mcaccessibility.tts.CustomCommandBackend;
import com.github.lostcra.mcaccessibility.tts.SpeechBackend;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.List;

public class AccessibilitySettingsScreen extends Screen {
    public static final int TAB_SYNTHESIZER = 0;
    public static final int TAB_SPEECH = 1;
    public static final int TAB_ADVANCED = 2;
    public static final int TAB_TUTORIAL = 3;

    private static int activeTab = TAB_SYNTHESIZER;

    private final Screen parent;
    private SpeechBackend currentBackend;
    private Button deleteButton;

    public AccessibilitySettingsScreen(Screen parent) {
        super(Component.translatable("mcaccessibility.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AccessibilityConfig config = AccessibilityConfig.get();
        this.currentBackend = SpeechEngine.getInstance().getActiveBackend(config);

        int cx = this.width / 2;

        // 1. Title
        addRenderableOnly(new StringWidget(cx - 155, 6, 310, 14,
                Component.translatable("mcaccessibility.settings.title"), this.font));

        // 2. Tab Navigation Bar (4 buttons: cx-155, cx-76, cx+3, cx+82)
        int tabY = 22;
        int tWidth1 = 76;
        int tWidth2 = 76;
        int tWidth3 = 76;
        int tWidth4 = 73;

        Button synthTabBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tab.synthesizer"),
                btn -> selectTab(TAB_SYNTHESIZER)
        ).bounds(cx - 155, tabY, tWidth1, 18).build());
        synthTabBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.tab.synthesizer.tooltip")));
        if (activeTab == TAB_SYNTHESIZER) synthTabBtn.active = false;

        Button speechTabBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tab.speech"),
                btn -> selectTab(TAB_SPEECH)
        ).bounds(cx - 76, tabY, tWidth2, 18).build());
        speechTabBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.tab.speech.tooltip")));
        if (activeTab == TAB_SPEECH) speechTabBtn.active = false;

        Button advTabBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tab.advanced"),
                btn -> selectTab(TAB_ADVANCED)
        ).bounds(cx + 3, tabY, tWidth3, 18).build());
        advTabBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.tab.advanced.tooltip")));
        if (activeTab == TAB_ADVANCED) advTabBtn.active = false;

        Button tutTabBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tab.tutorial"),
                btn -> openTutorial()
        ).bounds(cx + 82, tabY, tWidth4, 18).build());
        tutTabBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.tab.tutorial.tooltip")));

        // 3. Render contents of the currently selected tab
        int contentY = 46;
        switch (activeTab) {
            case TAB_SYNTHESIZER:
                initSynthesizerTab(cx, contentY, config);
                break;
            case TAB_SPEECH:
                initSpeechTab(cx, contentY, config);
                break;
            case TAB_ADVANCED:
                initAdvancedTab(cx, contentY, config);
                break;
            default:
                initSynthesizerTab(cx, contentY, config);
                break;
        }

        // 4. Bottom Row: Done Button (all tabs)
        int bottomY = this.height - 26;
        Button doneBtn = addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                btn -> onClose()
        ).bounds(cx - 100, bottomY, 200, 20).build());
        doneBtn.setTooltip(Tooltip.create(Component.literal("Сохранить и закрыть (Save & Close)")));
    }

    private void selectTab(int tabIndex) {
        if (tabIndex != activeTab) {
            activeTab = tabIndex;
            rebuildWidgets();
            String tabTitle;
            switch (tabIndex) {
                case TAB_SYNTHESIZER:
                    tabTitle = Component.translatable("mcaccessibility.tab.synthesizer").getString();
                    break;
                case TAB_SPEECH:
                    tabTitle = Component.translatable("mcaccessibility.tab.speech").getString();
                    break;
                case TAB_ADVANCED:
                    tabTitle = Component.translatable("mcaccessibility.tab.advanced").getString();
                    break;
                default:
                    tabTitle = "";
                    break;
            }
            SpeechEngine.getInstance().speak("Вкладка: " + tabTitle, true);
        }
    }

    private void openTutorial() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(new TutorialScreen(this));
        }
    }

    // ==========================================
    // TAB 0: SYNTHESIZER
    // ==========================================
    private void initSynthesizerTab(int cx, int y, AccessibilityConfig config) {
        List<SpeechBackend> backends = SpeechEngine.getInstance().getAvailableBackends();

        // 1. Synthesizer Selection (width 310)
        CycleButton<SpeechBackend> synthButton = CycleButton.builder(
                b -> Component.literal(b.getName()),
                this.currentBackend
        ).withValues(backends).create(
                cx - 155, y, 310, 20,
                Component.translatable("mcaccessibility.synth.select"),
                (btn, newBackend) -> {
                    this.currentBackend = newBackend;
                    config.setTtsEngine(newBackend.getId());
                    config.setSelectedVoice("Default");
                    config.save();
                    rebuildWidgets();
                    SpeechEngine.getInstance().speak(Component.translatable("mcaccessibility.synth.select").getString() + ": " + newBackend.getName(), true);
                }
        );
        synthButton.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.synth.select.tooltip")));
        addRenderableWidget(synthButton);
        y += 24;

        // 2. Voice Selection (150) & Test (150)
        Button voiceBtn = addRenderableWidget(createVoiceButton(cx - 155, y, 150, 20));

        Button testBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.gui.test"),
                btn -> SpeechEngine.getInstance().testSpeech()
        ).bounds(cx + 5, y, 150, 20).build());
        testBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.gui.test_tooltip")));
        y += 24;

        // 3. Add Synthesizer (150) & Delete Synthesizer (150)
        Button addBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.synth.add"),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new AddSynthesizerScreen(this));
                    }
                }
        ).bounds(cx - 155, y, 150, 20).build());
        addBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.synth.add_tooltip")));

        this.deleteButton = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.synth.delete"),
                btn -> confirmDeleteSynthesizer()
        ).bounds(cx + 5, y, 150, 20).build());
        updateDeleteButtonState();
        y += 28;

        // 4. Status Information Card
        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14,
                Component.literal("Активный движок: " + currentBackend.getName()), this.font));
        y += 16;

        String voiceInfo = config.getSelectedVoice() != null ? config.getSelectedVoice() : "Default";
        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14,
                Component.literal("Текущий голос: " + voiceInfo), this.font));
        y += 16;

        if (currentBackend instanceof CustomCommandBackend customBackend) {
            String path = customBackend.getConfig().getExecutablePath();
            addRenderableOnly(new StringWidget(cx - 155, y, 310, 14,
                    Component.literal("Исполняемый файл: " + truncate(path, 40)), this.font));
        } else {
            addRenderableOnly(new StringWidget(cx - 155, y, 310, 14,
                    Component.literal("Тип: Встроенный системный синтезатор речи"), this.font));
        }
    }

    // ==========================================
    // TAB 1: SPEECH SETTINGS
    // ==========================================
    private void initSpeechTab(int cx, int y, AccessibilityConfig config) {
        // 1. Rate & Volume Sliders (2 columns of 150)
        RateSlider rateSlider = addRenderableWidget(new RateSlider(cx - 155, y, 150, 20, config.getSpeechRate()));
        rateSlider.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.speech.rate_tooltip")));

        VolumeSlider volumeSlider = addRenderableWidget(new VolumeSlider(cx + 5, y, 150, 20, config.getSpeechVolume()));
        volumeSlider.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.speech.volume_tooltip")));
        y += 22;

        // 2. Master Toggle & Screen Narration (2 columns of 150)
        CycleButton<Boolean> masterBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isMasterEnabled()).create(
                cx - 155, y, 150, 20,
                Component.translatable("mcaccessibility.speech.master"),
                (btn, val) -> {
                    config.setMasterEnabled(val);
                    config.save();
                }
        ));
        masterBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.speech.master_tooltip")));

        CycleButton<Boolean> screenBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isScreenNarrationEnabled()).create(
                cx + 5, y, 150, 20,
                Component.translatable("mcaccessibility.speech.screen"),
                (btn, val) -> {
                    config.setScreenNarrationEnabled(val);
                    config.save();
                }
        ));
        screenBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.speech.screen_tooltip")));
        y += 22;

        // 3. Inventory Narration & Chat Narration (2 columns of 150)
        CycleButton<Boolean> invBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isInventoryNarrationEnabled()).create(
                cx - 155, y, 150, 20,
                Component.translatable("mcaccessibility.speech.inventory"),
                (btn, val) -> {
                    config.setInventoryNarrationEnabled(val);
                    config.save();
                }
        ));
        invBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.speech.inventory_tooltip")));

        CycleButton<Boolean> chatBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isChatNarrationEnabled()).create(
                cx + 5, y, 150, 20,
                Component.translatable("mcaccessibility.speech.chat"),
                (btn, val) -> {
                    config.setChatNarrationEnabled(val);
                    config.save();
                }
        ));
        chatBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.speech.chat_tooltip")));
        y += 22;

        // 4. F3 Narration & Speech Interrupt on Keypress (2 columns of 150)
        CycleButton<Boolean> f3Btn = addRenderableWidget(CycleButton.onOffBuilder(config.isF3NarrationEnabled()).create(
                cx - 155, y, 150, 20,
                Component.translatable("mcaccessibility.speech.f3"),
                (btn, val) -> {
                    config.setF3NarrationEnabled(val);
                    config.save();
                }
        ));
        f3Btn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.speech.f3_tooltip")));

        CycleButton<Boolean> interruptBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isInterruptOnKeyPress()).create(
                cx + 5, y, 150, 20,
                Component.translatable("mcaccessibility.speech.interrupt"),
                (btn, val) -> {
                    config.setInterruptOnKeyPress(val);
                    config.save();
                    SpeechEngine.getInstance().speak(val ? "Остановка речи клавишами включена" : "Остановка речи клавишами отключена", true);
                }
        ));
        interruptBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.speech.interrupt_tooltip")));
        y += 24;

        // 5. Test Speech button
        Button testBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.gui.test"),
                btn -> SpeechEngine.getInstance().testSpeech()
        ).bounds(cx - 155, y, 310, 20).build());
        testBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.gui.test_tooltip")));
    }

    // ==========================================
    // TAB 2: ADVANCED & SYNTHESIZER CONFIG
    // ==========================================
    private void initAdvancedTab(int cx, int y, AccessibilityConfig config) {
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
        y += 24;

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
    }

    private void openFolderForFile(String path) {
        if (path == null || path.trim().isEmpty()) return;
        try {
            File f = new File(path);
            if (f.exists()) {
                new ProcessBuilder("explorer.exe", "/select,", f.getAbsolutePath()).start();
                SpeechEngine.getInstance().speak("Папка программы открыта в Проводнике Windows", true);
                return;
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

    private void updateDeleteButtonState() {
        if (deleteButton != null) {
            boolean isCustom = currentBackend instanceof CustomCommandBackend;
            deleteButton.active = isCustom;
            if (isCustom) {
                deleteButton.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.synth.delete_tooltip", currentBackend.getName())));
            } else {
                deleteButton.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.synth.delete_builtin_tooltip")));
            }
        }
    }

    private void confirmDeleteSynthesizer() {
        if (!(currentBackend instanceof CustomCommandBackend customBackend)) {
            SpeechEngine.getInstance().speak("Нельзя удалить встроенный синтезатор", true);
            return;
        }

        String synthName = customBackend.getName();
        String synthId = customBackend.getConfig().getId();

        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) {
                            AccessibilityConfig config = AccessibilityConfig.get();
                            config.removeCustomSynthesizer(synthId);
                            config.setTtsEngine("WINDOWS_SAPI");
                            config.setSelectedVoice("Default");
                            config.save();

                            SpeechEngine.getInstance().reloadCustomBackends();
                            SpeechEngine.getInstance().speak("Синтезатор " + synthName + " успешно удалён.", true);

                            if (this.minecraft != null) {
                                this.minecraft.setScreenAndShow(new AccessibilitySettingsScreen(this.parent));
                            }
                        } else {
                            if (this.minecraft != null) {
                                this.minecraft.setScreenAndShow(this);
                            }
                        }
                    },
                    Component.translatable("mcaccessibility.synth.delete_confirm_title"),
                    Component.translatable("mcaccessibility.synth.delete_confirm_msg", synthName),
                    Component.translatable("mcaccessibility.synth.delete"),
                    CommonComponents.GUI_CANCEL
            ));
        }
    }

    private Button createVoiceButton(int x, int y, int width, int height) {
        AccessibilityConfig config = AccessibilityConfig.get();
        String curVoice = config.getSelectedVoice();
        Button btn = Button.builder(
                Component.translatable("mcaccessibility.voice.select", truncate(curVoice, 8)),
                b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new VoiceSelectionScreen(this));
                    }
                }
        ).bounds(x, y, width, height).build();
        btn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.voice.select.tooltip")));
        return btn;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "Default";
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == InputConstants.KEY_PAGEUP) {
            selectTab((activeTab - 1 + 3) % 3);
            return true;
        } else if (key == InputConstants.KEY_PAGEDOWN) {
            selectTab((activeTab + 1) % 3);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        AccessibilityConfig.get().save();
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }

    private static class RateSlider extends AbstractSliderButton {
        public RateSlider(int x, int y, int width, int height, int initialRate) {
            super(x, y, width, height, Component.empty(), (initialRate + 10.0) / 20.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int rate = (int) Math.round(this.value * 20.0) - 10;
            String rateStr = (rate > 0 ? "+" + rate : String.valueOf(rate));
            this.setMessage(Component.translatable("mcaccessibility.speech.rate").append(": " + rateStr));
        }

        @Override
        protected void applyValue() {
            int rate = (int) Math.round(this.value * 20.0) - 10;
            AccessibilityConfig.get().setSpeechRate(rate);
        }
    }

    private static class VolumeSlider extends AbstractSliderButton {
        public VolumeSlider(int x, int y, int width, int height, int initialVolume) {
            super(x, y, width, height, Component.empty(), initialVolume / 100.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int vol = (int) Math.round(this.value * 100.0);
            this.setMessage(Component.translatable("mcaccessibility.speech.volume").append(": " + vol + "%"));
        }

        @Override
        protected void applyValue() {
            int vol = (int) Math.round(this.value * 100.0);
            AccessibilityConfig.get().setSpeechVolume(vol);
        }
    }
}
