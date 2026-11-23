package com.github.lostcra.mcaccessibility.config;

import com.github.lostcra.mcaccessibility.tts.CustomCommandBackend;
import com.github.lostcra.mcaccessibility.tts.SpeechBackend;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AccessibilitySettingsScreen extends Screen {
    private final Screen parent;
    private Button voiceButton;
    private Button deleteButton;
    private SpeechBackend currentBackend;

    public AccessibilitySettingsScreen(Screen parent) {
        super(Component.literal("Настройки доступности и синтезатора"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AccessibilityConfig config = AccessibilityConfig.get();
        int cx = this.width / 2;
        int y = 24;

        // Title
        addRenderableOnly(new StringWidget(cx - 155, 6, 310, 16, Component.literal("Настройки доступности и синтезатора"), this.font));

        // 1. Synthesizer Selection (Full width 310)
        List<SpeechBackend> backends = SpeechEngine.getInstance().getAvailableBackends();
        this.currentBackend = SpeechEngine.getInstance().getActiveBackend(config);

        CycleButton<SpeechBackend> synthButton = CycleButton.builder(
                b -> Component.literal(b.getName()),
                this.currentBackend
        ).withValues(backends).create(
                cx - 155, y, 310, 20,
                Component.literal("Синтезатор"),
                (btn, newBackend) -> {
                    this.currentBackend = newBackend;
                    config.setTtsEngine(newBackend.getId());
                    config.setSelectedVoice("Default");
                    config.save();
                    refreshVoiceButton(cx - 155, 46, 150, 20);
                    updateDeleteButtonState();
                    SpeechEngine.getInstance().speak("Выбран синтезатор: " + newBackend.getName(), true);
                }
        );
        synthButton.setTooltip(Tooltip.create(Component.literal("Выбор активного синтезатора речи (Windows SAPI, eSpeak или Mojang Narrator).")));
        addRenderableWidget(synthButton);
        y += 22;

        // 2. Voice Selection (150) & Capital Letters Mode (150)
        this.voiceButton = addRenderableWidget(createVoiceButton(cx - 155, y, 150, 20));

        CycleButton<String> capitalBtn = addRenderableWidget(CycleButton.builder(
                mode -> {
                    switch (mode) {
                        case "OFF": return Component.literal("Выкл");
                        case "WORD": return Component.literal("Слово");
                        case "PITCH": return Component.literal("Тон");
                        case "BEEP": return Component.literal("Сигнал");
                        default: return Component.literal(mode);
                    }
                },
                config.getCapitalMode()
        ).withValues(List.of("OFF", "WORD", "PITCH", "BEEP")).create(
                cx + 5, y, 150, 20,
                Component.literal("Заглавные буквы"),
                (btn, val) -> {
                    config.setCapitalMode(val);
                    config.save();
                    SpeechEngine.getInstance().speak("Заглавные буквы: " + btn.getMessage().getString(), true);
                }
        ));
        capitalBtn.setTooltip(Tooltip.create(Component.literal("Индикация заглавных букв при навигации и вводе в текстовых полях:\n- Выкл: обычное чтение\n- Слово: проговаривать 'Заглавная'\n- Тон: более высокий тон голоса\n- Сигнал: звуковой щелчок")));
        y += 22;

        // 3. Sliders: Rate & Volume (2 columns of 150)
        RateSlider rateSlider = addRenderableWidget(new RateSlider(cx - 155, y, 150, 20, config.getSpeechRate()));
        rateSlider.setTooltip(Tooltip.create(Component.literal("Скорость речи активного синтезатора (от -10 до +10).")));

        VolumeSlider volumeSlider = addRenderableWidget(new VolumeSlider(cx + 5, y, 150, 20, config.getSpeechVolume()));
        volumeSlider.setTooltip(Tooltip.create(Component.literal("Громкость воспроизведения речи (от 0% до 100%).")));
        y += 22;

        // 4. Test Speech, Add Synthesizer, Delete Synthesizer (3 columns of 100, gap 5)
        Button testBtn = addRenderableWidget(Button.builder(
                Component.literal("Проверить"),
                btn -> SpeechEngine.getInstance().testSpeech()
        ).bounds(cx - 155, y, 100, 20).build());
        testBtn.setTooltip(Tooltip.create(Component.literal("Воспроизвести пробную фразу текущим выбранным синтезатором.")));

        Button addBtn = addRenderableWidget(Button.builder(
                Component.literal("Добавить..."),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new AddSynthesizerScreen(this));
                    }
                }
        ).bounds(cx - 50, y, 100, 20).build());
        addBtn.setTooltip(Tooltip.create(Component.literal("Добавить внешний синтезатор речи (eSpeak, RHVoice, Piper и др.).")));

        this.deleteButton = addRenderableWidget(Button.builder(
                Component.literal("Удалить"),
                btn -> confirmDeleteSynthesizer()
        ).bounds(cx + 55, y, 100, 20).build());
        updateDeleteButtonState();
        y += 22;

        // 5. Punctuation Mode (150) & Emoji Reading (150)
        CycleButton<String> punctBtn = addRenderableWidget(CycleButton.builder(
                mode -> {
                    switch (mode) {
                        case "NONE": return Component.literal("Выкл");
                        case "SOME": return Component.literal("Основная");
                        case "ALL": return Component.literal("Все знаки");
                        default: return Component.literal(mode);
                    }
                },
                config.getPunctuationMode()
        ).withValues(List.of("NONE", "SOME", "ALL")).create(
                cx - 155, y, 150, 20,
                Component.literal("Пунктуация"),
                (btn, val) -> {
                    config.setPunctuationMode(val);
                    config.save();
                    SpeechEngine.getInstance().speak("Пунктуация: " + btn.getMessage().getString(), true);
                }
        ));
        punctBtn.setTooltip(Tooltip.create(Component.literal("Чтение знаков препинания:\n- Выкл: знаки опускаются\n- Основная: точка, запятая, вопрос, восклицание, двоеточие\n- Все знаки: все символы, включая скобки, слэши, решётки")));

        CycleButton<Boolean> emojiBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isReadEmojis()).create(
                cx + 5, y, 150, 20,
                Component.literal("Чтение смайликов"),
                (btn, val) -> {
                    config.setReadEmojis(val);
                    config.save();
                    SpeechEngine.getInstance().speak(val ? "Чтение смайликов включено" : "Чтение смайликов отключено", true);
                }
        ));
        emojiBtn.setTooltip(Tooltip.create(Component.literal("Озвучивание смайликов и эмодзи словами (например :) как 'смайлик улыбка', ❤️ как 'красное сердце').")));
        y += 22;

        // 6. Master Toggle & Screen Narration (2 columns)
        CycleButton<Boolean> masterBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isMasterEnabled()).create(
                cx - 155, y, 150, 20,
                Component.literal("Озвучка игры"),
                (btn, val) -> {
                    config.setMasterEnabled(val);
                    config.save();
                }
        ));
        masterBtn.setTooltip(Tooltip.create(Component.literal("Главный переключатель всей озвучки игры (горячая клавиша: Ctrl+Alt+A).")));

        CycleButton<Boolean> screenBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isScreenNarrationEnabled()).create(
                cx + 5, y, 150, 20,
                Component.literal("Озвучка экранов"),
                (btn, val) -> {
                    config.setScreenNarrationEnabled(val);
                    config.save();
                }
        ));
        screenBtn.setTooltip(Tooltip.create(Component.literal("Озвучивание меню, кнопок, переключателей и экранов интерфейса.")));
        y += 22;

        // 7. Inventory Narration & Chat Narration (2 columns)
        CycleButton<Boolean> invBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isInventoryNarrationEnabled()).create(
                cx - 155, y, 150, 20,
                Component.literal("Озвучка инвентаря"),
                (btn, val) -> {
                    config.setInventoryNarrationEnabled(val);
                    config.save();
                }
        ));
        invBtn.setTooltip(Tooltip.create(Component.literal("Озвучивание предметов, слотов и количества в инвентаре и сундуках.")));

        CycleButton<Boolean> chatBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isChatNarrationEnabled()).create(
                cx + 5, y, 150, 20,
                Component.literal("Чтение чата"),
                (btn, val) -> {
                    config.setChatNarrationEnabled(val);
                    config.save();
                }
        ));
        chatBtn.setTooltip(Tooltip.create(Component.literal("Озвучивание сообщений игрового чата (горячая клавиша: Ctrl+Alt+C).")));
        y += 22;

        // 8. Detailed Tooltips & Narrate Hotkeys (2 columns)
        CycleButton<Boolean> tooltipsBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isDetailedTooltips()).create(
                cx - 155, y, 150, 20,
                Component.literal("Подробные чары"),
                (btn, val) -> {
                    config.setDetailedTooltips(val);
                    config.save();
                }
        ));
        tooltipsBtn.setTooltip(Tooltip.create(Component.literal("Подробное озвучивание свойств, зачарований и описаний предметов.")));

        CycleButton<Boolean> hotkeysBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isNarrateHotkeys()).create(
                cx + 5, y, 150, 20,
                Component.literal("Озвучка клавиш"),
                (btn, val) -> {
                    config.setNarrateHotkeys(val);
                    config.save();
                }
        ));
        hotkeysBtn.setTooltip(Tooltip.create(Component.literal("Озвучивать голосовые подтверждения при нажатии горячих клавиш (Ctrl+Alt+A/C/S/F).")));
        y += 22;

        // 9. F3 Narration & Speech Interrupt on Keypress (2 columns of 150)
        CycleButton<Boolean> f3Btn = addRenderableWidget(CycleButton.onOffBuilder(config.isF3NarrationEnabled()).create(
                cx - 155, y, 150, 20,
                Component.literal("Озвучка меню F3"),
                (btn, val) -> {
                    config.setF3NarrationEnabled(val);
                    config.save();
                }
        ));
        f3Btn.setTooltip(Tooltip.create(Component.literal("Озвучивание координат X Y Z, стороны света и биома по клавише Ctrl+Alt+F.")));

        CycleButton<Boolean> interruptBtn = addRenderableWidget(CycleButton.onOffBuilder(config.isInterruptOnKeyPress()).create(
                cx + 5, y, 150, 20,
                Component.literal("Остановка речи"),
                (btn, val) -> {
                    config.setInterruptOnKeyPress(val);
                    config.save();
                    SpeechEngine.getInstance().speak(val ? "Остановка речи клавишами включена" : "Остановка речи клавишами отключена", true);
                }
        ));
        interruptBtn.setTooltip(Tooltip.create(Component.literal("Остановка речи клавишами:\nПри включении нажатие любой клавиши (или Ctrl) мгновенно прерывает текущую речь синтезатора.")));
        y += 24;

        // 10. Done Button (centered 200)
        Button doneBtn = addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                btn -> onClose()
        ).bounds(cx - 100, y, 200, 20).build());
        doneBtn.setTooltip(Tooltip.create(Component.literal("Сохранить настройки и закрыть меню.")));
    }

    private void updateDeleteButtonState() {
        if (deleteButton != null) {
            boolean isCustom = currentBackend instanceof CustomCommandBackend;
            deleteButton.active = isCustom;
            if (isCustom) {
                deleteButton.setTooltip(Tooltip.create(Component.literal("Удалить выбранный синтезатор: " + currentBackend.getName())));
            } else {
                deleteButton.setTooltip(Tooltip.create(Component.literal("Нельзя удалить встроенный системный синтезатор")));
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
                    Component.literal("Удаление синтезатора"),
                    Component.literal("Вы действительно хотите удалить синтезатор \"" + synthName + "\"?"),
                    Component.literal("Удалить"),
                    CommonComponents.GUI_CANCEL
            ));
        }
    }

    private Button createVoiceButton(int x, int y, int width, int height) {
        AccessibilityConfig config = AccessibilityConfig.get();
        String curVoice = config.getSelectedVoice();
        Button btn = Button.builder(
                Component.literal("Голос: " + truncate(curVoice, 8) + "…"),
                b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new VoiceSelectionScreen(this));
                    }
                }
        ).bounds(x, y, width, height).build();
        btn.setTooltip(Tooltip.create(Component.literal("Выбор голоса: " + curVoice + "\nНажмите для открытия экрана со списком доступных голосов и поиском.")));
        return btn;
    }

    private void refreshVoiceButton(int x, int y, int width, int height) {
        if (voiceButton != null) {
            removeWidget(voiceButton);
        }
        this.voiceButton = addRenderableWidget(createVoiceButton(x, y, width, height));
    }

    private static String truncate(String text, int max) {
        if (text == null) return "Default";
        return text.length() <= max ? text : text.substring(0, max - 1);
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
            this.setMessage(Component.literal("Скорость: " + (rate > 0 ? "+" + rate : String.valueOf(rate))));
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
            this.setMessage(Component.literal("Громкость: " + vol + "%"));
        }

        @Override
        protected void applyValue() {
            int vol = (int) Math.round(this.value * 100.0);
            AccessibilityConfig.get().setSpeechVolume(vol);
        }
    }
}
