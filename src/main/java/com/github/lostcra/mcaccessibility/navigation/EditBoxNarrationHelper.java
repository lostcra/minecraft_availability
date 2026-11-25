package com.github.lostcra.mcaccessibility.navigation;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.mixin.AccessorEditBox;
import com.github.lostcra.mcaccessibility.mixin.AccessorScreen;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import com.github.lostcra.mcaccessibility.tts.SpeechTextProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;

import java.lang.ref.WeakReference;

public class EditBoxNarrationHelper {
    private static String savedValue = "";
    private static int savedCursor = 0;

    private static WeakReference<EditBox> lastNarratedBoxRef = new WeakReference<>(null);
    private static long lastNarratedTime = 0;
    private static WeakReference<EditBox> activeEditBoxRef = new WeakReference<>(null);
    private static long lastEditTime = 0;

    public static boolean consumeSuppressScreenNarration() {
        return false;
    }

    public static void onScreenRemoved() {
        activeEditBoxRef = new WeakReference<>(null);
        lastNarratedBoxRef = new WeakReference<>(null);
        lastEditTime = 0;
    }

    public static boolean isRussianLocale() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getLanguageManager() != null) {
                String code = mc.getLanguageManager().getSelected();
                return code != null && code.toLowerCase().startsWith("ru");
            }
        } catch (Throwable ignored) {}
        return true;
    }

    public static String getEditBoxLabel(EditBox box) {
        if (box == null) return "";
        Component msg = box.getMessage();
        String label = (msg != null) ? msg.getString().trim() : "";

        if (label.isEmpty()) {
            try {
                if (box instanceof AccessorEditBox) {
                    Component hintComp = ((AccessorEditBox) box).mcaccess$getHint();
                    if (hintComp != null) {
                        label = hintComp.getString().trim();
                    }
                }
            } catch (Throwable ignored) {}
        }

        if (label.isEmpty()) {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.gui != null) {
                    Screen screen = mc.gui.screen();
                    if (screen != null) {
                        Component title = screen.getTitle();
                        if (title != null && !title.getString().trim().isEmpty()) {
                            label = title.getString().trim();
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return label;
    }

    public static String getEditBoxDescription(EditBox box) {
        if (box == null) return "";
        String label = getEditBoxLabel(box);

        Component hintComp = null;
        try {
            if (box instanceof AccessorEditBox) {
                hintComp = ((AccessorEditBox) box).mcaccess$getHint();
            }
        } catch (Throwable ignored) {}
        String hint = (hintComp != null) ? hintComp.getString().trim() : "";

        if (hint.equalsIgnoreCase(label)) {
            hint = "";
        }

        String val = box.getValue() != null ? box.getValue() : "";
        boolean isRussian = isRussianLocale();

        if (isRussian) {
            String typeName = "текстовое поле";
            if (val.isEmpty()) {
                if (!hint.isEmpty()) {
                    return label.isEmpty()
                            ? typeName + ", пусто, подсказка: " + hint
                            : label + ", " + typeName + ", пусто, подсказка: " + hint;
                } else {
                    return label.isEmpty()
                            ? typeName + ", пусто"
                            : label + ", " + typeName + ", пусто";
                }
            } else {
                return label.isEmpty()
                        ? typeName + ": " + val
                        : label + ", " + typeName + ": " + val;
            }
        } else {
            String typeName = "edit box";
            if (val.isEmpty()) {
                if (!hint.isEmpty()) {
                    return label.isEmpty()
                            ? typeName + ", empty, hint: " + hint
                            : label + ", " + typeName + ", empty, hint: " + hint;
                } else {
                    return label.isEmpty()
                            ? typeName + ", empty"
                            : label + ", " + typeName + ", empty";
                }
            } else {
                return label.isEmpty()
                        ? typeName + ": " + val
                        : label + ", " + typeName + ": " + val;
            }
        }
    }

    public static MutableComponent buildNarrationMessage(EditBox box) {
        String desc = getEditBoxDescription(box);
        return Component.literal(desc);
    }

    public static boolean isScreenInitializing() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gui != null) {
                Screen screen = mc.gui.screen();
                if (screen instanceof AccessorScreen) {
                    return !((AccessorScreen) screen).mcaccess$isInitialized();
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static void onSetFocused(EditBox box, boolean focused, boolean wasFocused) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }

        if (focused && !wasFocused) {
            recordFocusGained(box);
            if (!isScreenInitializing()) {
                narrateEditBox(box, true);
            }
        } else if (!focused && wasFocused) {
            if (activeEditBoxRef.get() == box) {
                activeEditBoxRef = new WeakReference<>(null);
                lastEditTime = 0;
            }
        }
    }

    public static void recordFocusGained(EditBox box) {
        activeEditBoxRef = new WeakReference<>(box);
        lastNarratedBoxRef = new WeakReference<>(box);
        lastNarratedTime = System.currentTimeMillis();
        lastEditTime = 0;
    }

    public static void narrateEditBox(EditBox box, boolean interrupt) {
        recordFocusGained(box);
        String desc = getEditBoxDescription(box);
        if (!desc.isEmpty()) {
            SpeechEngine.getInstance().speak(desc, interrupt);
        }
    }

    public static boolean isRecentlyEdited() {
        return activeEditBoxRef.get() != null && (System.currentTimeMillis() - lastEditTime < 800L);
    }

    public static boolean shouldSuppressScreenNarration(String text) {
        // 1. If actively typing or editing in an EditBox within the last 800ms, suppress delayed screen narration
        if (isRecentlyEdited()) {
            return true;
        }

        // 2. If the text is for an EditBox that was already narrated on focus change within the last 1000ms:
        EditBox lastBox = lastNarratedBoxRef.get();
        if (lastBox != null && (System.currentTimeMillis() - lastNarratedTime < 1000L)) {
            String desc = getEditBoxDescription(lastBox);
            String label = getEditBoxLabel(lastBox);
            if (text != null) {
                if ((!desc.isEmpty() && text.contains(desc))
                        || (!label.isEmpty() && text.contains(label))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void beforeKeyPressed(EditBox box) {
        if (box != null) {
            savedValue = box.getValue() != null ? box.getValue() : "";
            savedCursor = box.getCursorPosition();
            activeEditBoxRef = new WeakReference<>(box);
        }
    }

    public static void beforeCharTyped(EditBox box) {
        if (box != null) {
            savedValue = box.getValue() != null ? box.getValue() : "";
            savedCursor = box.getCursorPosition();
            activeEditBoxRef = new WeakReference<>(box);
        }
    }

    public static boolean handleCustomShortcut(EditBox box, KeyEvent event) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return false;
        }

        int shortcut = event.shortcutKey();
        int key = event.key();
        boolean hasCtrl = event.hasControlDown();
        boolean isRussian = isRussianLocale();

        // Ctrl+R or Ctrl+Up or Ctrl+Down: Read the full text of the edit box and cursor position!
        if (hasCtrl && (key == 82 || shortcut == 82 || key == 265 || key == 264 || shortcut == 1073741906 || shortcut == 1073741905)) {
            activeEditBoxRef = new WeakReference<>(box);
            lastEditTime = System.currentTimeMillis();
            String val = box.getValue() != null ? box.getValue() : "";
            int cursor = box.getCursorPosition();
            if (val.isEmpty()) {
                SpeechEngine.getInstance().speak(isRussian ? "Текст пуст" : "Text is empty", true);
            } else {
                String msg = (isRussian ? "Текст: " : "Text: ") + val +
                        (isRussian ? ", длина: " : ", length: ") + val.length() +
                        (isRussian ? ", курсор: " : ", cursor: ") + cursor;
                SpeechEngine.getInstance().speak(msg, true);
            }
            return true;
        }

        return false;
    }

    public static void afterKeyPressed(EditBox box, KeyEvent event) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }

        activeEditBoxRef = new WeakReference<>(box);
        lastEditTime = System.currentTimeMillis();

        int shortcut = event.shortcutKey();
        int key = event.key();
        boolean hasShift = event.hasShiftDown();
        boolean hasCtrl = event.hasControlDown();
        boolean isRussian = isRussianLocale();

        String newVal = box.getValue() != null ? box.getValue() : "";
        int newCursor = box.getCursorPosition();

        // 1. Left Arrow (shortcut 1073741904 or key 263)
        if (shortcut == 1073741904 || key == 263) {
            if (hasCtrl) {
                // Word navigation left
                if (newCursor != savedCursor) {
                    int start = Math.max(0, Math.min(newVal.length(), Math.min(savedCursor, newCursor)));
                    int end = Math.max(0, Math.min(newVal.length(), Math.max(savedCursor, newCursor)));
                    String word = newVal.substring(start, end).trim();
                    SpeechEngine.getInstance().speak(word.isEmpty() ? (isRussian ? "Пробел" : "Space") : word, true);
                } else if (newCursor <= 0) {
                    SpeechEngine.getInstance().speak(isRussian ? "Начало строки" : "Start of line", true);
                }
            } else if (!hasShift) {
                // Character navigation left
                if (newCursor != savedCursor && newCursor >= 0 && newCursor < newVal.length()) {
                    char c = newVal.charAt(newCursor);
                    playCapitalBeepIfNeeded(c, config);
                    SpeechEngine.getInstance().speak(SpeechTextProcessor.narrateCharacter(c, config.getCapitalMode()), true);
                } else if (newCursor <= 0 && savedCursor <= 0) {
                    SpeechEngine.getInstance().speak(isRussian ? "Начало строки" : "Start of line", true);
                }
            }
            return;
        }

        // 2. Right Arrow (shortcut 1073741903 or key 262)
        if (shortcut == 1073741903 || key == 262) {
            if (hasCtrl) {
                // Word navigation right
                if (newCursor != savedCursor) {
                    int start = Math.max(0, Math.min(newVal.length(), Math.min(savedCursor, newCursor)));
                    int end = Math.max(0, Math.min(newVal.length(), Math.max(savedCursor, newCursor)));
                    String word = newVal.substring(start, end).trim();
                    SpeechEngine.getInstance().speak(word.isEmpty() ? (isRussian ? "Пробел" : "Space") : word, true);
                } else if (newCursor >= newVal.length()) {
                    SpeechEngine.getInstance().speak(isRussian ? "Конец строки" : "End of line", true);
                }
            } else if (!hasShift) {
                // Character navigation right
                if (newCursor >= 0 && newCursor < newVal.length()) {
                    char c = newVal.charAt(newCursor);
                    playCapitalBeepIfNeeded(c, config);
                    SpeechEngine.getInstance().speak(SpeechTextProcessor.narrateCharacter(c, config.getCapitalMode()), true);
                } else if (newCursor >= newVal.length()) {
                    SpeechEngine.getInstance().speak(isRussian ? "Конец строки" : "End of line", true);
                }
            }
            return;
        }

        // 3. Home (shortcut 1073741898 or key 268)
        if (shortcut == 1073741898 || key == 268) {
            if (newVal.isEmpty()) {
                SpeechEngine.getInstance().speak(isRussian ? "Начало строки, пусто" : "Start of line, empty", true);
            } else {
                char c = newVal.charAt(0);
                playCapitalBeepIfNeeded(c, config);
                SpeechEngine.getInstance().speak((isRussian ? "Начало строки: " : "Start of line: ") + SpeechTextProcessor.narrateCharacter(c, config.getCapitalMode()), true);
            }
            return;
        }

        // 4. End (shortcut 1073741901 or key 269)
        if (shortcut == 1073741901 || key == 269) {
            SpeechEngine.getInstance().speak(isRussian ? "Конец строки" : "End of line", true);
            return;
        }

        // 5. Up / Down arrow when consumed (e.g. Chat history cycling)
        if (key == 265 || key == 264) {
            SpeechEngine.getInstance().speak(newVal.isEmpty() ? (isRussian ? "Пусто" : "Empty") : newVal, true);
            return;
        }

        // 6. Backspace (shortcut 8 or key 259)
        if (shortcut == 8 || key == 259) {
            if (savedValue.length() > newVal.length()) {
                int diff = savedValue.length() - newVal.length();
                if (diff == 1 && newCursor >= 0 && newCursor < savedValue.length()) {
                    char del = savedValue.charAt(newCursor);
                    SpeechEngine.getInstance().speak((isRussian ? "Удалено " : "Deleted ") + SpeechTextProcessor.narrateCharacter(del, config.getCapitalMode()), true);
                } else {
                    SpeechEngine.getInstance().speak((isRussian ? "Удалено символов: " : "Deleted characters: ") + diff, true);
                }
            } else if (savedValue.isEmpty()) {
                SpeechEngine.getInstance().speak(isRussian ? "Пусто" : "Empty", true);
            }
            return;
        }

        // 7. Delete (shortcut 127 or key 261)
        if (shortcut == 127 || key == 261) {
            if (savedValue.length() > newVal.length()) {
                int diff = savedValue.length() - newVal.length();
                if (diff == 1 && savedCursor >= 0 && savedCursor < savedValue.length()) {
                    char del = savedValue.charAt(savedCursor);
                    SpeechEngine.getInstance().speak((isRussian ? "Удалено " : "Deleted ") + SpeechTextProcessor.narrateCharacter(del, config.getCapitalMode()), true);
                } else {
                    SpeechEngine.getInstance().speak((isRussian ? "Удалено символов: " : "Deleted characters: ") + diff, true);
                }
            } else if (newCursor >= newVal.length()) {
                SpeechEngine.getInstance().speak(isRussian ? "Конец строки" : "End of line", true);
            }
            return;
        }

        // 8. Ctrl+A (Select All)
        if (hasCtrl && (key == 65 || shortcut == 65 || shortcut == 1073741889)) {
            SpeechEngine.getInstance().speak((isRussian ? "Выделен весь текст, символов: " : "All text selected, length: ") + newVal.length(), true);
            return;
        }

        // 9. Ctrl+C (Copy)
        if (hasCtrl && (key == 67 || shortcut == 67)) {
            SpeechEngine.getInstance().speak(isRussian ? "Скопировано" : "Copied", true);
            return;
        }

        // 10. Ctrl+V (Paste)
        if (hasCtrl && (key == 86 || shortcut == 86)) {
            SpeechEngine.getInstance().speak((isRussian ? "Вставлено: " : "Pasted: ") + newVal, true);
            return;
        }

        // 11. Ctrl+X (Cut)
        if (hasCtrl && (key == 88 || shortcut == 88)) {
            SpeechEngine.getInstance().speak(isRussian ? "Вырезано" : "Cut", true);
            return;
        }
    }

    public static void afterCharTyped(EditBox box, CharacterEvent event) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        activeEditBoxRef = new WeakReference<>(box);
        lastEditTime = System.currentTimeMillis();

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
        activeEditBoxRef = new WeakReference<>(box);
        lastEditTime = System.currentTimeMillis();

        // If the box was not narrated recently, announce the field
        if (lastNarratedBoxRef.get() != box || (System.currentTimeMillis() - lastNarratedTime > 600L)) {
            narrateEditBox(box, true);
            return;
        }

        // If already announced, announce position inside box
        int pos = box.getCursorPosition();
        String val = box.getValue() != null ? box.getValue() : "";
        boolean isRussian = isRussianLocale();
        if (pos >= 0 && pos < val.length()) {
            char c = val.charAt(pos);
            playCapitalBeepIfNeeded(c, config);
            SpeechEngine.getInstance().speak(SpeechTextProcessor.narrateCharacter(c, config.getCapitalMode()), true);
        } else {
            SpeechEngine.getInstance().speak(isRussian ? "Конец строки" : "End of line", true);
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
