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
import java.util.List;
import java.util.UUID;

public class AddSynthesizerScreen extends Screen {
    private final Screen parent;

    private EditBox nameBox;
    private EditBox pathBox;
    private EditBox argsBox;
    private boolean useStdin = true;
    private int detectedIndex = 0;

    public AddSynthesizerScreen(Screen parent) {
        super(Component.translatable("mcaccessibility.add_synth.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 24;

        addRenderableOnly(new StringWidget(cx - 155, 6, 310, 16, Component.translatable("mcaccessibility.add_synth.title"), this.font));

        // 1. Synthesizer Name
        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14, Component.translatable("mcaccessibility.add_synth.name_label"), this.font));
        y += 14;
        this.nameBox = addRenderableWidget(new EditBox(this.font, cx - 155, y, 310, 20, Component.translatable("mcaccessibility.add_synth.name_label")));
        this.nameBox.setHint(Component.translatable("mcaccessibility.add_synth.name_hint"));
        this.nameBox.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.add_synth.name_tooltip")));
        this.nameBox.setMaxLength(64);
        y += 22;

        // 2. Executable Path or Folder & Browse button
        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14, Component.translatable("mcaccessibility.add_synth.path_label"), this.font));
        y += 14;
        this.pathBox = addRenderableWidget(new EditBox(this.font, cx - 155, y, 210, 20, Component.translatable("mcaccessibility.add_synth.path_label")));
        this.pathBox.setHint(Component.translatable("mcaccessibility.add_synth.path_hint"));
        this.pathBox.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.add_synth.path_tooltip")));
        this.pathBox.setMaxLength(256);

        Button browseBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.add_synth.browse"),
                btn -> openBrowseDialog()
        ).bounds(cx + 60, y, 95, 20).build());
        browseBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.add_synth.browse_tooltip")));
        y += 22;

        // 3. Auto-Detect & Run Installer buttons
        Button autoDetectBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.add_synth.autodetect"),
                btn -> autoDetectSynthesizer()
        ).bounds(cx - 155, y, 150, 20).build());
        autoDetectBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.add_synth.autodetect_tooltip")));

        Button installBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.add_synth.install"),
                btn -> handleInstallButton()
        ).bounds(cx + 5, y, 150, 20).build());
        installBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.add_synth.install_tooltip")));
        y += 22;

        // 4. Arguments
        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14, Component.translatable("mcaccessibility.synth_edit.args_label"), this.font));
        y += 14;
        this.argsBox = addRenderableWidget(new EditBox(this.font, cx - 155, y, 310, 20, Component.translatable("mcaccessibility.synth_edit.args_label")));
        this.argsBox.setHint(Component.literal("Для eSpeak: -v ru -b 1 (или оставьте пустым)"));
        this.argsBox.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.synth_edit.args_tooltip")));
        this.argsBox.setMaxLength(256);
        y += 22;

        // 5. Mode selector (stdin vs command line arguments)
        CycleButton<Boolean> stdinBtn = addRenderableWidget(CycleButton.onOffBuilder(true)
                .create(cx - 155, y, 310, 20, Component.translatable("mcaccessibility.synth_edit.stdin_toggle"),
                        (btn, val) -> this.useStdin = val));
        stdinBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.synth_edit.stdin_tooltip")));
        y += 24;

        // 6. Test & Save buttons
        Button testBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.gui.test"),
                btn -> testCustomSynthesizer()
        ).bounds(cx - 155, y, 150, 20).build());
        testBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.gui.test_tooltip")));

        Button saveBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.gui.save"),
                btn -> saveCustomSynthesizer()
        ).bounds(cx + 5, y, 150, 20).build());
        saveBtn.setTooltip(Tooltip.create(Component.translatable("mcaccessibility.gui.test_tooltip")));
        y += 24;

        // 7. Back Button
        Button backBtn = addRenderableWidget(Button.builder(
                CommonComponents.GUI_BACK,
                btn -> onClose()
        ).bounds(cx - 100, y, 200, 20).build());
        backBtn.setTooltip(Tooltip.create(Component.literal("Назад: вернуться в меню настроек без сохранения.")));
    }

    private void openBrowseDialog() {
        SpeechEngine.getInstance().speak("Открыто системное окно выбора файла", true);
        SynthesizerFinder.openBrowseDialog(selectedPath -> {
            this.pathBox.setValue(selectedPath);
            File f = new File(selectedPath);
            if (SynthesizerFinder.isInstaller(f)) {
                SpeechEngine.getInstance().speak("Выбран файл установщика: " + f.getName() + ". Нажмите кнопку 'Установить', чтобы запустить установку.", true);
            } else {
                File resolved = SynthesizerFinder.resolveExecutable(f);
                if (resolved != null) {
                    this.pathBox.setValue(resolved.getAbsolutePath());
                    fillDefaultsForFile(resolved);
                    SpeechEngine.getInstance().speak("Выбран синтезатор: " + resolved.getName(), true);
                } else {
                    SpeechEngine.getInstance().speak("Выбран путь: " + selectedPath, true);
                }
            }
        });
    }

    private void autoDetectSynthesizer() {
        List<SynthesizerFinder.DiscoveredSynthesizer> all = SynthesizerFinder.findAll();
        if (all.isEmpty()) {
            SpeechEngine.getInstance().speak("Установленные синтезаторы речи в реестре и стандартных папках не найдены. Укажите папку или файл установщика вручную.", true);
            return;
        }

        if (detectedIndex >= all.size()) {
            detectedIndex = 0;
        }
        SynthesizerFinder.DiscoveredSynthesizer synth = all.get(detectedIndex);
        detectedIndex = (detectedIndex + 1) % all.size();

        this.nameBox.setValue(synth.getName());
        this.pathBox.setValue(synth.getExecutablePath());
        this.argsBox.setValue(synth.getArgsPattern());
        this.useStdin = synth.isUseStdin();

        String countMsg = all.size() > 1 ? " (найдено всего: " + all.size() + ", нажмите ещё раз для переключения)" : "";
        SpeechEngine.getInstance().speak("Обнаружен синтезатор: " + synth.getName() + countMsg, true);
    }

    private void handleInstallButton() {
        String path = pathBox.getValue().trim();
        File f = new File(path);

        if (!path.isEmpty() && f.exists() && SynthesizerFinder.isInstaller(f)) {
            runInstaller(f);
            return;
        }

        SpeechEngine.getInstance().speak("Выберите файл установщика (setup.exe) для запуска установки", true);
        SynthesizerFinder.openBrowseDialog(selectedPath -> {
            File chosen = new File(selectedPath);
            this.pathBox.setValue(selectedPath);
            runInstaller(chosen);
        });
    }

    private void runInstaller(File installerFile) {
        SpeechEngine.getInstance().speak("Запуск мастера установки. Пожалуйста, выполните установку программы в открывшемся окне...", true);

        SynthesizerFinder.launchInstallerAndCapture(
                installerFile,
                detected -> {
                    this.nameBox.setValue(detected.getName());
                    this.pathBox.setValue(detected.getExecutablePath());
                    this.argsBox.setValue(detected.getArgsPattern());
                    this.useStdin = detected.isUseStdin();
                    SpeechEngine.getInstance().speak("Установка завершена! Программа успешно обнаружена: " + detected.getName() + " в папке: " + detected.getExecutablePath() + ". Настройки применены.", true);
                },
                status -> SpeechEngine.getInstance().speak(status, true)
        );
    }

    private void fillDefaultsForFile(File exe) {
        String lower = exe.getAbsolutePath().toLowerCase();
        if (nameBox.getValue().trim().isEmpty() || nameBox.getValue().equalsIgnoreCase("Тест")) {
            if (lower.contains("espeak-ng")) {
                nameBox.setValue("eSpeak NG");
            } else if (lower.contains("espeak")) {
                nameBox.setValue("eSpeak");
            } else if (lower.contains("rhvoice")) {
                nameBox.setValue("RHVoice");
            } else if (lower.contains("piper")) {
                nameBox.setValue("Piper");
            }
        }
        if (argsBox.getValue().trim().isEmpty()) {
            if (lower.contains("espeak-ng")) {
                argsBox.setValue("-v ru");
            } else if (lower.contains("espeak")) {
                argsBox.setValue("-v ru -b 1");
            } else if (lower.contains("rhvoice")) {
                argsBox.setValue("-p Aleksandr");
            }
        }
    }

    private void testCustomSynthesizer() {
        String rawPath = pathBox.getValue().trim();
        if (rawPath.isEmpty()) {
            SpeechEngine.getInstance().speak("Укажите путь к исполняемому файлу, папке или установщику", true);
            return;
        }

        File target = new File(rawPath);
        if (target.exists() && SynthesizerFinder.isInstaller(target)) {
            SpeechEngine.getInstance().speak("Указан файл установщика! Нажмите кнопку 'Установить', чтобы выполнить установку.", true);
            return;
        }

        File exe = SynthesizerFinder.resolvePathOrFolder(rawPath);
        if (exe == null || !exe.exists()) {
            SpeechEngine.getInstance().speak("Исполняемый файл синтезатора по указанному пути или внутри указанной папки не найден", true);
            return;
        }

        this.pathBox.setValue(exe.getAbsolutePath());
        fillDefaultsForFile(exe);

        CustomSynthesizerConfig temp = new CustomSynthesizerConfig(
                "TEMP",
                nameBox.getValue().trim().isEmpty() ? "Тест" : nameBox.getValue().trim(),
                exe.getAbsolutePath(),
                argsBox.getValue().trim(),
                useStdin
        );
        CustomCommandBackend testBackend = new CustomCommandBackend(temp);
        testBackend.speak("Тестовое воспроизведение пользовательского синтезатора речи выполнено успешно.", true, 100, 0, "Default");
    }

    private void saveCustomSynthesizer() {
        String name = nameBox.getValue().trim();
        String rawPath = pathBox.getValue().trim();

        if (name.isEmpty()) {
            SpeechEngine.getInstance().speak("Введите название синтезатора", true);
            return;
        }
        if (rawPath.isEmpty()) {
            SpeechEngine.getInstance().speak("Укажите путь к файлу или папке программы", true);
            return;
        }

        File target = new File(rawPath);
        if (target.exists() && SynthesizerFinder.isInstaller(target)) {
            SpeechEngine.getInstance().speak("Указан файл установщика! Нажмите кнопку 'Установить', чтобы установить и автоматически подключить программу.", true);
            return;
        }

        File exe = SynthesizerFinder.resolvePathOrFolder(rawPath);
        if (exe == null || !exe.exists()) {
            SpeechEngine.getInstance().speak("Исполняемый файл программы по указанному пути или в указанной папке не найден", true);
            return;
        }

        this.pathBox.setValue(exe.getAbsolutePath());

        String id = "SYNTH_" + UUID.randomUUID().toString().substring(0, 8);
        CustomSynthesizerConfig cfg = new CustomSynthesizerConfig(id, name, exe.getAbsolutePath(), argsBox.getValue().trim(), useStdin);
        AccessibilityConfig config = AccessibilityConfig.get();
        config.addCustomSynthesizer(cfg);
        config.setTtsEngine("CUSTOM_" + id);
        config.setSelectedVoice("Default");
        config.save();

        SpeechEngine.getInstance().reloadCustomBackends();
        SpeechEngine.getInstance().speak("Синтезатор " + name + " успешно сохранён и подключён к игре!", true);

        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }
}
