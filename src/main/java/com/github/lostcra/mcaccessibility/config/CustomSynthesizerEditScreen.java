package com.github.lostcra.mcaccessibility.config;

import com.github.lostcra.mcaccessibility.tts.CustomCommandBackend;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.File;

public class CustomSynthesizerEditScreen extends Screen {
    private final Screen parent;
    private final CustomSynthesizerConfig synthConfig;

    private EditBox argsBox;
    private boolean useStdin;

    public CustomSynthesizerEditScreen(Screen parent, CustomSynthesizerConfig synthConfig) {
        super(Component.translatable("mcaccessibility.synth_edit.title"));
        this.parent = parent;
        this.synthConfig = synthConfig;
        this.useStdin = synthConfig.isUseStdin();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 24;

        // Title
        addRenderableOnly(new StringWidget(cx - 155, 6, 310, 16,
                Component.translatable("mcaccessibility.synth_edit.title"), this.font));

        // Synthesizer Name & Path info
        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14,
                Component.literal("Синтезатор: " + synthConfig.getName()), this.font));
        y += 16;

        String pathStr = synthConfig.getExecutablePath() != null ? synthConfig.getExecutablePath() : "";
        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14,
                Component.literal("Путь: " + truncate(pathStr, 45)), this.font));
        y += 20;

        // Arguments pattern EditBox
        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14,
                Component.translatable("mcaccessibility.synth_edit.args_label"), this.font));
        y += 14;

        this.argsBox = addRenderableWidget(new EditBox(this.font, cx - 155, y, 310, 20,
                Component.translatable("mcaccessibility.synth_edit.args_label")));
        this.argsBox.setValue(synthConfig.getArgsPattern() != null ? synthConfig.getArgsPattern() : "");
        this.argsBox.setHint(Component.literal("-v ru -b 1"));
        this.argsBox.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.synth_edit.args_tooltip")));
        this.argsBox.setMaxLength(256);
        y += 24;

        // Stdin toggle
        CycleButton<Boolean> stdinBtn = addRenderableWidget(CycleButton.onOffBuilder(this.useStdin)
                .create(cx - 155, y, 310, 20,
                        Component.translatable("mcaccessibility.synth_edit.stdin_toggle"),
                        (btn, val) -> this.useStdin = val));
        stdinBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.synth_edit.stdin_tooltip")));
        y += 24;

        // Action Buttons: Open Folder, Test
        Button openFolderBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.synth_edit.open_folder"),
                btn -> openExecutableFolder()
        ).bounds(cx - 155, y, 150, 20).build());
        openFolderBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.synth_edit.open_folder_tooltip")));

        Button testBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.gui.test"),
                btn -> testSettings()
        ).bounds(cx + 5, y, 150, 20).build());
        testBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.gui.test_tooltip")));
        y += 26;

        // Bottom: Save, Cancel
        Button saveBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.gui.save"),
                btn -> saveAndClose()
        ).bounds(cx - 155, y, 150, 20).build());

        Button cancelBtn = addRenderableWidget(Button.builder(
                CommonComponents.GUI_CANCEL,
                btn -> onClose()
        ).bounds(cx + 5, y, 150, 20).build());
    }

    private void openExecutableFolder() {
        try {
            if (synthConfig.getExecutablePath() != null) {
                File exe = new File(synthConfig.getExecutablePath());
                if (exe.exists()) {
                    File folder = exe.isDirectory() ? exe : exe.getParentFile();
                    String os = System.getProperty("os.name", "").toLowerCase();
                    if (os.contains("win")) {
                        new ProcessBuilder("explorer.exe", "/select,", exe.getAbsolutePath()).start();
                        SpeechEngine.getInstance().speak("Папка программы открыта в Проводнике Windows", true);
                        return;
                    } else if (os.contains("mac")) {
                        new ProcessBuilder("open", "-R", exe.getAbsolutePath()).start();
                        SpeechEngine.getInstance().speak("Папка программы открыта в Finder", true);
                        return;
                    } else {
                        new ProcessBuilder("xdg-open", folder != null ? folder.getAbsolutePath() : exe.getAbsolutePath()).start();
                        SpeechEngine.getInstance().speak("Папка программы открыта в файловом менеджере", true);
                        return;
                    }
                }
            }
            SpeechEngine.getInstance().speak("Файл программы не найден на диске", true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void testSettings() {
        String newArgs = argsBox.getValue().trim();
        CustomSynthesizerConfig temp = new CustomSynthesizerConfig(
                synthConfig.getId(),
                synthConfig.getName(),
                synthConfig.getExecutablePath(),
                newArgs,
                useStdin
        );
        CustomCommandBackend testBackend = new CustomCommandBackend(temp);
        testBackend.speak("Тест пользовательских параметров синтезатора речи.", true, 100, 0, "Default");
    }

    private void saveAndClose() {
        synthConfig.setArgsPattern(argsBox.getValue().trim());
        synthConfig.setUseStdin(this.useStdin);

        AccessibilityConfig config = AccessibilityConfig.get();
        config.save();
        SpeechEngine.getInstance().reloadCustomBackends();
        SpeechEngine.getInstance().speak("Параметры синтезатора сохранены", true);

        onClose();
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : "…" + text.substring(text.length() - max);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }
}
