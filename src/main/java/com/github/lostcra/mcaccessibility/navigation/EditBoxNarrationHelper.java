package com.github.lostcra.mcaccessibility.navigation;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import com.github.lostcra.mcaccessibility.tts.SpeechTextProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class EditBoxNarrationHelper {
    private static boolean suppressScreenNarration = false;
    private static String savedValue = "";
    private static int savedCursor = 0;

    public static boolean consumeSuppressScreenNarration() {
        boolean val = suppressScreenNarration;
        suppressScreenNarration = false;
        return val;
    }

    public static void beforeKeyPressed(EditBox box) {
        if (box != null) {
            savedValue = box.getValue() != null ? box.getValue() : "";
            savedCursor = box.getCursorPosition();
        }
    }

    public static void beforeCharTyped(EditBox box) {
        if (box != null) {
            savedValue = box.getValue() != null ? box.getValue() : "";
            savedCursor = box.getCursorPosition();
        }
    }

    public static void afterKeyPressed(EditBox box, KeyEvent event) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }

        int shortcut = event.shortcutKey();
        int key = event.key();
        boolean hasShift = event.hasShiftDown();
        boolean hasCtrl = event.hasControlDown();

        String newVal = box.getValue() != null ? box.getValue() : "";
        int newCursor = box.getCursorPosition();

        // 1. Left Arrow (shortcut 1073741904 or key 263)
        if (shortcut == 1073741904 || key == 263) {
            suppressScreenNarration = true;
            if (hasCtrl) {
                // Word navigation left
                if (newCursor != savedCursor) {
                    int start = Math.min(savedCursor, newCursor);
                    int end = Math.max(savedCursor, newCursor);
                    String word = newVal.substring(start, end).trim();
                    SpeechEngine.getInstance().speak(word.isEmpty() ? "Пробел" : word, true);
                } else if (newCursor == 0) {
                    SpeechEngine.getInstance().speak("Начало строки", true);
                }
            } else if (!hasShift) {
                // Character navigation left
                if (newCursor != savedCursor && newCursor < newVal.length()) {
                    char c = newVal.charAt(newCursor);
                    playCapitalBeepIfNeeded(c, config);
                    SpeechEngine.getInstance().speak(SpeechTextProcessor.narrateCharacter(c, config.getCapitalMode()), true);
                } else if (newCursor == 0 && savedCursor == 0) {
                    SpeechEngine.getInstance().speak("Начало строки", true);
                }
            }
            return;
        }

        // 2. Right Arrow (shortcut 1073741903 or key 262)
        if (shortcut == 1073741903 || key == 262) {
            suppressScreenNarration = true;
            if (hasCtrl) {
                // Word navigation right
                if (newCursor != savedCursor) {
                    int start = Math.min(savedCursor, newCursor);
                    int end = Math.max(savedCursor, newCursor);
                    String word = newVal.substring(start, end).trim();
                    SpeechEngine.getInstance().speak(word.isEmpty() ? "Пробел" : word, true);
                } else if (newCursor >= newVal.length()) {
                    SpeechEngine.getInstance().speak("Конец строки", true);
                }
            } else if (!hasShift) {
                // Character navigation right
                if (newCursor < newVal.length()) {
                    char c = newVal.charAt(newCursor);
                    playCapitalBeepIfNeeded(c, config);
                    SpeechEngine.getInstance().speak(SpeechTextProcessor.narrateCharacter(c, config.getCapitalMode()), true);
                } else if (newCursor == newVal.length()) {
                    SpeechEngine.getInstance().speak("Конец строки", true);
                }
            }
            return;
        }

        // 3. Home (shortcut 1073741898 or key 268)
        if (shortcut == 1073741898 || key == 268) {
            suppressScreenNarration = true;
            if (newVal.isEmpty()) {
                SpeechEngine.getInstance().speak("Начало строки, пусто", true);
            } else {
                char c = newVal.charAt(0);
                playCapitalBeepIfNeeded(c, config);
                SpeechEngine.getInstance().speak("Начало строки: " + SpeechTextProcessor.narrateCharacter(c, config.getCapitalMode()), true);
            }
            return;
        }

        // 4. End (shortcut 1073741901 or key 269)
        if (shortcut == 1073741901 || key == 269) {
            suppressScreenNarration = true;
            SpeechEngine.getInstance().speak("Конец строки", true);
            return;
        }

        // 5. Up / Down arrow (keys 265 / 264)
        if (key == 265 || key == 264) {
            suppressScreenNarration = true;
            SpeechEngine.getInstance().speak(newVal.isEmpty() ? "Пусто" : newVal, true);
            return;
        }

        // 6. Backspace (shortcut 8 or key 259)
        if (shortcut == 8 || key == 259) {
            suppressScreenNarration = true;
            if (savedValue.length() > newVal.length()) {
                int diff = savedValue.length() - newVal.length();
                if (diff == 1 && newCursor < savedValue.length()) {
                    char del = savedValue.charAt(newCursor);
                    SpeechEngine.getInstance().speak("Удалено " + SpeechTextProcessor.narrateCharacter(del, config.getCapitalMode()), true);
                } else {
                    SpeechEngine.getInstance().speak("Удалено символов: " + diff, true);
                }
            } else if (savedValue.isEmpty()) {
                SpeechEngine.getInstance().speak("Пусто", true);
            }
            return;
        }

        // 7. Delete (shortcut 127 or key 261)
        if (shortcut == 127 || key == 261) {
            suppressScreenNarration = true;
            if (savedValue.length() > newVal.length()) {
                int diff = savedValue.length() - newVal.length();
                if (diff == 1 && savedCursor < savedValue.length()) {
                    char del = savedValue.charAt(savedCursor);
                    SpeechEngine.getInstance().speak("Удалено " + SpeechTextProcessor.narrateCharacter(del, config.getCapitalMode()), true);
                } else {
                    SpeechEngine.getInstance().speak("Удалено символов: " + diff, true);
                }
            } else if (newCursor >= newVal.length()) {
                SpeechEngine.getInstance().speak("Конец строки", true);
            }
            return;
        }

        // 8. Ctrl+A (Select All)
        if (hasCtrl && (key == 65 || shortcut == 65 || shortcut == 1073741889)) {
            suppressScreenNarration = true;
            SpeechEngine.getInstance().speak("Выделен весь текст, символов: " + newVal.length(), true);
            return;
        }

        // 9. Ctrl+C (Copy)
        if (hasCtrl && (key == 67 || shortcut == 67)) {
            suppressScreenNarration = true;
            SpeechEngine.getInstance().speak("Скопировано", true);
            return;
        }

        // 10. Ctrl+V (Paste)
        if (hasCtrl && (key == 86 || shortcut == 86)) {
            suppressScreenNarration = true;
            SpeechEngine.getInstance().speak("Вставлено: " + newVal, true);
            return;
        }

        // 11. Ctrl+X (Cut)
        if (hasCtrl && (key == 88 || shortcut == 88)) {
            suppressScreenNarration = true;
            SpeechEngine.getInstance().speak("Вырезано", true);
            return;
        }
    }

    public static void afterCharTyped(EditBox box, CharacterEvent event) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        suppressScreenNarration = true;
        String str = event.codepointAsString();
        if (str != null && !str.isEmpty()) {
            char c = str.charAt(0);
            playCapitalBeepIfNeeded(c, config);
            SpeechEngine.getInstance().speak(SpeechTextProcessor.narrateCharacter(c, config.getCapitalMode()), true);
        }
    }

    public static void afterClick(EditBox box) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        suppressScreenNarration = true;
        int pos = box.getCursorPosition();
        String val = box.getValue() != null ? box.getValue() : "";
        if (pos < val.length()) {
            char c = val.charAt(pos);
            playCapitalBeepIfNeeded(c, config);
            SpeechEngine.getInstance().speak(SpeechTextProcessor.narrateCharacter(c, config.getCapitalMode()), true);
        } else {
            SpeechEngine.getInstance().speak("Конец строки", true);
        }
    }

    private static void playCapitalBeepIfNeeded(char c, AccessibilityConfig config) {
        if ("BEEP".equalsIgnoreCase(config.getCapitalMode()) && Character.isUpperCase(c)) {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.getSoundManager() != null) {
                    mc.execute(() -> mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 2.0F)));
                }
            } catch (Throwable ignored) {}
        }
    }
}
