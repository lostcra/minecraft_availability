package com.github.lostcra.mcaccessibility.config;

import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AccessibilityConfigScreen {

    public static Screen create(Screen parent) {
        try {
            return createClothConfigScreen(parent);
        } catch (Throwable t) {
            t.printStackTrace();
            return new FallbackConfigScreen(parent);
        }
    }

    private static Screen createClothConfigScreen(Screen parent) {
        AccessibilityConfig config = AccessibilityConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Настройки доступности (Minecraft Availability)"));

        ConfigEntryBuilder entry = builder.entryBuilder();

        // 1. General Category
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("Общие настройки"));

        general.addEntry(entry.startBooleanToggle(Component.literal("Включить озвучку (Ctrl+Alt+A)"), config.isMasterEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(config::setMasterEnabled)
                .setTooltip(Component.literal("Глобальный переключатель всей озвучки мода"))
                .build());

        general.addEntry(entry.startBooleanToggle(Component.literal("Озвучивать чат (Ctrl+Alt+C)"), config.isChatNarrationEnabled())
                .setDefaultValue(false)
                .setSaveConsumer(config::setChatNarrationEnabled)
                .setTooltip(Component.literal("Автоматически читать входящие сообщения чата"))
                .build());

        general.addEntry(entry.startBooleanToggle(Component.literal("Озвучивать заголовки экранов"), config.isScreenNarrationEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(config::setScreenNarrationEnabled)
                .setTooltip(Component.literal("Произносить название открываемого меню или окна"))
                .build());

        general.addEntry(entry.startBooleanToggle(Component.literal("Озвучивать инвентарь и контейнеры"), config.isInventoryNarrationEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(config::setInventoryNarrationEnabled)
                .setTooltip(Component.literal("Озвучивать слоты и предметы при наведении и навигации"))
                .build());

        general.addEntry(entry.startBooleanToggle(Component.literal("Подробные подсказки предметов (чары, прочность)"), config.isDetailedTooltips())
                .setDefaultValue(true)
                .setSaveConsumer(config::setDetailedTooltips)
                .build());

        general.addEntry(entry.startBooleanToggle(Component.literal("Клавиатурная навигация по инвентарю (стрелки, Tab, Пробел)"), config.isKeyboardInventoryNav())
                .setDefaultValue(true)
                .setSaveConsumer(config::setKeyboardInventoryNav)
                .build());

        general.addEntry(entry.startBooleanToggle(Component.literal("Клавиатурная навигация по достижениям"), config.isKeyboardAdvancementsNav())
                .setDefaultValue(true)
                .setSaveConsumer(config::setKeyboardAdvancementsNav)
                .build());

        // 2. Speech Synthesizer Category
        ConfigCategory tts = builder.getOrCreateCategory(Component.literal("Синтезатор речи"));

        String[] engines = new String[]{"MOJANG", "WINDOWS_SAPI"};
        tts.addEntry(entry.startSelector(Component.literal("Движок синтеза речи"), engines, config.getTtsEngine())
                .setDefaultValue("MOJANG")
                .setSaveConsumer(config::setTtsEngine)
                .setTooltip(Component.literal("MOJANG: стандартный диктор. WINDOWS_SAPI: системные голоса Windows (Irina, Zira и др.)"))
                .build());

        List<String> voices = SpeechEngine.getInstance().getAvailableVoices();
        String[] voiceArr = voices.toArray(new String[0]);
        String curVoice = voices.contains(config.getSelectedVoice()) ? config.getSelectedVoice() : "Default";

        tts.addEntry(entry.startSelector(Component.literal("Выбор голоса"), voiceArr, curVoice)
                .setDefaultValue("Default")
                .setSaveConsumer(config::setSelectedVoice)
                .setTooltip(Component.literal("Голос для синтеза речи (доступно при выборе Windows SAPI)"))
                .build());

        tts.addEntry(entry.startIntSlider(Component.literal("Скорость речи"), config.getSpeechRate(), -10, 10)
                .setDefaultValue(0)
                .setSaveConsumer(config::setSpeechRate)
                .build());

        tts.addEntry(entry.startIntSlider(Component.literal("Громкость озвучки"), config.getSpeechVolume(), 0, 100)
                .setDefaultValue(100)
                .setSaveConsumer(config::setSpeechVolume)
                .build());

        general.addEntry(entry.startTextDescription(Component.literal("Нажмите 'Проверить голос' в игре или используйте /accessibility test"))
                .build());

        builder.setSavingRunnable(() -> {
            config.save();
            SpeechEngine.getInstance().speak("Настройки сохранены", true);
        });

        return builder.build();
    }

    public static class FallbackConfigScreen extends Screen {
        private final Screen parent;

        public FallbackConfigScreen(Screen parent) {
            super(Component.literal("Настройки доступности"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            AccessibilityConfig config = AccessibilityConfig.get();
            int cx = this.width / 2;
            int y = 40;

            addRenderableWidget(Button.builder(
                    Component.literal("Озвучка: " + (config.isMasterEnabled() ? "ВКЛ" : "ВЫКЛ")),
                    btn -> {
                        config.setMasterEnabled(!config.isMasterEnabled());
                        config.save();
                        btn.setMessage(Component.literal("Озвучка: " + (config.isMasterEnabled() ? "ВКЛ" : "ВЫКЛ")));
                    }).bounds(cx - 100, y, 200, 20).build());

            y += 24;
            addRenderableWidget(Button.builder(
                    Component.literal("Чтение чата: " + (config.isChatNarrationEnabled() ? "ВКЛ" : "ВЫКЛ")),
                    btn -> {
                        config.setChatNarrationEnabled(!config.isChatNarrationEnabled());
                        config.save();
                        btn.setMessage(Component.literal("Чтение чата: " + (config.isChatNarrationEnabled() ? "ВКЛ" : "ВЫКЛ")));
                    }).bounds(cx - 100, y, 200, 20).build());

            y += 24;
            addRenderableWidget(Button.builder(
                    Component.literal("Движок: " + config.getTtsEngine()),
                    btn -> {
                        String next = "MOJANG".equals(config.getTtsEngine()) ? "WINDOWS_SAPI" : "MOJANG";
                        config.setTtsEngine(next);
                        config.save();
                        btn.setMessage(Component.literal("Движок: " + config.getTtsEngine()));
                    }).bounds(cx - 100, y, 200, 20).build());

            y += 24;
            addRenderableWidget(Button.builder(
                    Component.literal("Проверить голос"),
                    btn -> SpeechEngine.getInstance().testSpeech()
            ).bounds(cx - 100, y, 200, 20).build());

            y += 32;
            addRenderableWidget(Button.builder(
                    Component.literal("Готово"),
                    btn -> {
                        if (minecraft != null) {
                            minecraft.setScreenAndShow(parent);
                        }
                    }
            ).bounds(cx - 100, y, 200, 20).build());
        }

        @Override
        public void onClose() {
            if (minecraft != null) {
                minecraft.setScreenAndShow(parent);
            }
        }
    }
}
