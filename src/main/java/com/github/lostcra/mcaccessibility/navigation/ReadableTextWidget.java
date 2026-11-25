package com.github.lostcra.mcaccessibility.navigation;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Lightweight invisible focusable widget representing non-widget text rendered directly on a Screen
 * (such as MultiLineLabel messages in BackupConfirmScreen, AlertScreen, ErrorScreen, etc.).
 * Renders only a subtle focus outline when keyboard-focused, and narrates the supplied text.
 */
public class ReadableTextWidget extends AbstractWidget {
    private final Supplier<Component> textSupplier;

    public ReadableTextWidget(int x, int y, int width, int height, Supplier<Component> textSupplier) {
        super(x, y, Math.max(width, 20), Math.max(height, 14), Component.empty());
        this.textSupplier = textSupplier;
        this.active = false;
    }

    public Component getReadableText() {
        Component msg = textSupplier != null ? textSupplier.get() : Component.empty();
        return msg != null ? msg : Component.empty();
    }

    @Override
    public boolean isActive() {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!this.visible || !config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return false;
        }
        return !getReadableText().getString().trim().isEmpty();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // Silent: non-clickable text block
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.visible && this.isFocused()) {
            graphics.outline(getX() - 2, getY() - 2, getWidth() + 4, getHeight() + 4, 0xFFFFFFFF);
        }
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!this.visible || !config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return null;
        }
        String text = getReadableText().getString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return !this.isFocused() ? ComponentPath.leaf(this) : null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        Component text = getReadableText();
        if (!text.getString().trim().isEmpty()) {
            output.add(NarratedElementType.TITLE, text);
        }
    }
}
