package com.github.lostcra.mcaccessibility.navigation;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.mixin.AccessorGuiTextRenderState;
import com.github.lostcra.mcaccessibility.mixin.AccessorScreen;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Central helper for making non-button screen text accessible across all screens:
 * - Makes StringWidget / MultiLineTextWidget focusable and narratable (skipping redundant screen titles).
 * - Adds ReadableTextWidget elements to legacy MultiLineLabel screens.
 * - Captures rendered non-interactive text on demand (Ctrl+Alt+R) for any screen.
 */
public final class ScreenTextHelper {
    private static boolean captureArmed = false;
    private static boolean capturingFrame = false;
    private static WeakReference<Screen> captureTargetScreenRef = new WeakReference<>(null);
    private static int interactiveDepth = 0;
    private static final List<CapturedLine> capturedLines = new ArrayList<>();

    private record CapturedLine(int y, int x, String text) {}

    private ScreenTextHelper() {}

    public static boolean isPassiveTextWidget(GuiEventListener listener) {
        return (listener instanceof AbstractStringWidget && !(listener instanceof FocusableTextWidget))
                || listener instanceof ReadableTextWidget;
    }

    public static boolean shouldFocusTextWidget(AbstractStringWidget widget) {
        if (widget == null || !widget.visible) {
            return false;
        }
        Component msg = widget.getMessage();
        if (msg == null) {
            return false;
        }
        String text = msg.getString().trim();
        if (text.isEmpty()) {
            return false;
        }
        // Avoid adding an extra Tab stop for a single-line header StringWidget that merely duplicates screen.getTitle().
        if (widget instanceof StringWidget) {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc != null && mc.gui != null ? mc.gui.screen() : null;
            if (screen != null && screen.getTitle() != null) {
                String title = screen.getTitle().getString().trim();
                if (!title.isEmpty() && title.equalsIgnoreCase(text)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void addReadableText(Screen screen, int x, int y, int width, int height, java.util.function.Supplier<Component> supplier) {
        if (screen == null) return;
        ReadableTextWidget widget = new ReadableTextWidget(x, y, width, height, supplier);
        AccessorScreen acc = (AccessorScreen) screen;
        if (!acc.mcaccess$getChildren().contains(widget)) {
            acc.mcaccess$getChildren().add(0, widget);
        }
        if (!acc.mcaccess$getNarratables().contains(widget)) {
            acc.mcaccess$getNarratables().add(0, widget);
        }
    }

    public static <T extends GuiEventListener & NarratableEntry> void registerFocusableWidget(Screen screen, T widget) {
        if (screen == null || widget == null) return;
        AccessorScreen acc = (AccessorScreen) screen;
        if (!acc.mcaccess$getChildren().contains(widget)) {
            acc.mcaccess$getChildren().add(widget);
        }
        if (!acc.mcaccess$getNarratables().contains(widget)) {
            acc.mcaccess$getNarratables().add(widget);
        }
    }

    public static void ensureRenderableTextRegistered(Screen screen, Renderable renderable) {
        if (screen != null && renderable instanceof AbstractStringWidget textWidget
                && !(renderable instanceof FocusableTextWidget)) {
            registerFocusableWidget(screen, textWidget);
        }
    }

    /**
     * Collects body/warning text from ReadableTextWidget, MultiLineTextWidget, FocusableTextWidget,
     * and FittingMultiLineTextWidget that is not already included in {@code existingNarration}.
     */
    public static String collectBodyNarration(Screen screen, String existingNarration) {
        if (screen == null) return "";
        String normExisting = normalize(existingNarration);
        List<String> extraParts = new ArrayList<>();
        Set<Object> visited = new HashSet<>();
        collectBodyFromContainer(screen, normExisting, extraParts, visited);
        return String.join(". ", extraParts);
    }

    private static void collectBodyFromContainer(ContainerEventHandler container, String normExisting,
                                                 List<String> out, Set<Object> visited) {
        if (container == null || !visited.add(container)) return;
        for (GuiEventListener child : container.children()) {
            if (child instanceof ReadableTextWidget readable) {
                addIfNovel(readable.getReadableText().getString(), normExisting, out);
            } else if (child instanceof MultiLineTextWidget multiLine) {
                addIfNovel(multiLine.getMessage().getString(), normExisting, out);
            } else if (child instanceof FittingMultiLineTextWidget fitting) {
                addIfNovel(fitting.getMessage().getString(), normExisting, out);
            } else if (child instanceof ContainerEventHandler nested) {
                collectBodyFromContainer(nested, normExisting, out, visited);
            }
        }
    }

    private static void addIfNovel(String rawText, String normExisting, List<String> out) {
        if (rawText == null) return;
        String trimmed = rawText.trim();
        if (trimmed.isEmpty()) return;
        String norm = normalize(trimmed);
        if (norm.isEmpty()) return;
        if (normExisting.contains(norm)) return;
        for (String existing : out) {
            if (normalize(existing).contains(norm)) {
                return;
            }
        }
        out.add(trimmed);
    }

    private static String normalize(String text) {
        if (text == null) return "";
        return text.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    // =========================================================================
    // On-demand universal screen text reader (Ctrl + Alt + R)
    // =========================================================================

    public static void requestReadCurrentScreen(Minecraft mc) {
        if (mc == null || mc.gui == null) return;
        Screen screen = mc.gui.screen();
        if (screen == null) return;
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled()) return;

        captureTargetScreenRef = new WeakReference<>(screen);
        captureArmed = true;
        capturingFrame = false;
        interactiveDepth = 0;
        capturedLines.clear();
    }

    public static void onScreenRemoved(Screen screen) {
        if (screen != null && screen == captureTargetScreenRef.get()) {
            captureArmed = false;
            capturingFrame = false;
            captureTargetScreenRef = new WeakReference<>(null);
            interactiveDepth = 0;
            capturedLines.clear();
        }
    }

    public static void onScreenRenderStart(Screen screen) {
        if (captureArmed && screen == captureTargetScreenRef.get()) {
            captureArmed = false;
            capturingFrame = true;
            interactiveDepth = 0;
            capturedLines.clear();
        }
    }

    public static void onScreenRenderEnd(Screen screen) {
        if (!capturingFrame || screen != captureTargetScreenRef.get()) {
            return;
        }
        capturingFrame = false;
        captureTargetScreenRef = new WeakReference<>(null);
        interactiveDepth = 0;

        String text = buildCapturedSummary(screen);
        capturedLines.clear();
        if (!text.isEmpty()) {
            SpeechEngine.getInstance().speak(text, true);
        } else {
            String title = screen.getTitle() != null ? screen.getTitle().getString().trim() : "";
            if (!title.isEmpty()) {
                SpeechEngine.getInstance().speak(title, true);
            } else {
                SpeechEngine.getInstance().speak("На экране нет дополнительного текста.", true);
            }
        }
    }

    public static boolean isCapturingFrame() {
        return capturingFrame;
    }

    public static void pushWidgetRender(AbstractWidget widget) {
        if (!capturingFrame) return;
        if (isInteractiveWidget(widget)) {
            interactiveDepth++;
        }
    }

    public static void popWidgetRender(AbstractWidget widget) {
        if (!capturingFrame) return;
        if (isInteractiveWidget(widget) && interactiveDepth > 0) {
            interactiveDepth--;
        }
    }

    public static void pushSelectionListRender() {
        if (capturingFrame) {
            interactiveDepth++;
        }
    }

    public static void popSelectionListRender() {
        if (capturingFrame && interactiveDepth > 0) {
            interactiveDepth--;
        }
    }

    private static boolean isInteractiveWidget(AbstractWidget widget) {
        if (widget instanceof AbstractStringWidget || widget instanceof FittingMultiLineTextWidget) {
            return false;
        }
        return widget instanceof AbstractButton
                || widget instanceof AbstractSliderButton
                || widget instanceof EditBox
                || widget instanceof AbstractSelectionList;
    }

    public static void recordGuiText(GuiTextRenderState state) {
        if (!capturingFrame || interactiveDepth > 0 || state == null) {
            return;
        }
        AccessorGuiTextRenderState acc = (AccessorGuiTextRenderState) (Object) state;
        FormattedCharSequence seq = acc.mcaccess$getText();
        if (seq == null) return;
        String text = extractString(seq).trim();
        if (!text.isEmpty()) {
            capturedLines.add(new CapturedLine(acc.mcaccess$getY(), acc.mcaccess$getX(), text));
        }
    }

    private static String extractString(FormattedCharSequence seq) {
        StringBuilder sb = new StringBuilder();
        seq.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }

    private static String buildCapturedSummary(Screen screen) {
        Set<String> lines = new LinkedHashSet<>();

        // Include ReadableTextWidgets first if present, since their text is structured in complete paragraphs.
        List<String> structured = new ArrayList<>();
        Set<Object> visited = new HashSet<>();
        collectStructuredText(screen, structured, visited);
        for (String s : structured) {
            lines.add(s);
        }

        // Also merge rendered non-interactive lines sorted top-to-bottom, left-to-right.
        capturedLines.sort(Comparator.comparingInt(CapturedLine::y).thenComparingInt(CapturedLine::x));
        StringBuilder currentParagraph = new StringBuilder();
        int lastY = Integer.MIN_VALUE;
        for (CapturedLine line : capturedLines) {
            String t = line.text();
            if (isAlreadyCovered(t, structured)) {
                continue;
            }
            if (lastY != Integer.MIN_VALUE && Math.abs(line.y() - lastY) <= 13) {
                if (currentParagraph.length() > 0) currentParagraph.append(' ');
                currentParagraph.append(t);
            } else {
                if (currentParagraph.length() > 0) {
                    lines.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                }
                currentParagraph.append(t);
            }
            lastY = line.y();
        }
        if (currentParagraph.length() > 0) {
            lines.add(currentParagraph.toString().trim());
        }

        return String.join(". ", lines);
    }

    private static void collectStructuredText(ContainerEventHandler container, List<String> out, Set<Object> visited) {
        if (container == null || !visited.add(container)) return;
        for (GuiEventListener child : container.children()) {
            if (child instanceof ReadableTextWidget readable) {
                String t = readable.getReadableText().getString().trim();
                if (!t.isEmpty()) out.add(t);
            } else if (child instanceof AbstractStringWidget sw) {
                String t = sw.getMessage().getString().trim();
                if (!t.isEmpty()) out.add(t);
            } else if (child instanceof FittingMultiLineTextWidget fitting) {
                String t = fitting.getMessage().getString().trim();
                if (!t.isEmpty()) out.add(t);
            } else if (child instanceof ContainerEventHandler nested) {
                collectStructuredText(nested, out, visited);
            }
        }
    }

    private static boolean isAlreadyCovered(String line, List<String> structured) {
        String normLine = normalize(line);
        if (normLine.isEmpty()) return true;
        for (String s : structured) {
            if (normalize(s).contains(normLine)) {
                return true;
            }
        }
        return false;
    }
}
