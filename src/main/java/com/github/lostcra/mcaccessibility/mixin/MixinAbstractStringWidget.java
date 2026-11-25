package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractStringWidget.class)
public abstract class MixinAbstractStringWidget extends AbstractWidget {
    protected MixinAbstractStringWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "updateWidgetNarration", at = @At("HEAD"))
    private void onUpdateWidgetNarration(NarrationElementOutput output, CallbackInfo ci) {
        if ((Object) this instanceof FocusableTextWidget) {
            return;
        }
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        Component msg = this.getMessage();
        if (msg != null && !msg.getString().trim().isEmpty()) {
            output.add(NarratedElementType.TITLE, msg);
        }
    }
}
