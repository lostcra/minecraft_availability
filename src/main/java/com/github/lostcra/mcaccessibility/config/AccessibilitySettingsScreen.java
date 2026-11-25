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

import java.util.List;

public class AccessibilitySettingsScreen extends Screen {
    public static final int TAB_SYNTHESIZER = 0;
    public static final int TAB_SPEECH = 1;

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
        if (activeTab != TAB_SYNTHESIZER && activeTab != TAB_SPEECH) {
            activeTab = TAB_SYNTHESIZER;
        }

        int cx = this.width / 2;

        // 1. Title
        addRenderableOnly(new StringWidget(cx - 155, 6, 310, 14,
                Component.translatable("mcaccessibility.settings.title"), this.font));

        // 2. Tab Navigation Bar (3 buttons: cx-155, cx-50, cx+55)
        int tabY = 22;
        int tWidth = 100;

        Button synthTabBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tab.synthesizer"),
                btn -> selectTab(TAB_SYNTHESIZER)
        ).bounds(cx - 155, tabY, tWidth, 18).build());
        synthTabBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.tab.synthesizer.tooltip")));
        if (activeTab == TAB_SYNTHESIZER) synthTabBtn.active = false;

        Button speechTabBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tab.speech"),
                btn -> selectTab(TAB_SPEECH)
        ).bounds(cx - 50, tabY, tWidth, 18).build());
        speechTabBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.tab.speech.tooltip")));
        if (activeTab == TAB_SPEECH) speechTabBtn.active = false;

        Button tutTabBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tab.tutorial"),
                btn -> openTutorial()
        ).bounds(cx + 55, tabY, tWidth, 18).build());
        tutTabBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.tab.tutorial.tooltip")));

        // 3. Render contents of the currently selected tab
        int contentY = 46;
        switch (activeTab) {
            case TAB_SPEECH:
                initSpeechTab(cx, contentY, config);
                break;
            case TAB_SYNTHESIZER:
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

        // 5. Advanced Settings & Test Speech buttons (2 columns of 150)
        Button advancedBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.speech.advanced"),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new AdvancedSettingsScreen(this));
                    }
                }
        ).bounds(cx - 155, y, 150, 20).build());
        advancedBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.speech.advanced_tooltip")));

        Button testBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.gui.test"),
                btn -> SpeechEngine.getInstance().testSpeech()
        ).bounds(cx + 5, y, 150, 20).build());
        testBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.gui.test_tooltip")));
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
            selectTab((activeTab - 1 + 2) % 2);
            return true;
        } else if (key == InputConstants.KEY_PAGEDOWN) {
            selectTab((activeTab + 1) % 2);
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
