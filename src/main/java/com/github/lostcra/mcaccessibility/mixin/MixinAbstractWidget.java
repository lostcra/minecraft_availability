package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.navigation.ScreenTextHelper;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractWidget.class)
public abstract class MixinAbstractWidget {
    @Shadow public abstract boolean isFocused();
    @Shadow public abstract int getX();
    @Shadow public abstract int getY();
    @Shadow public abstract int getWidth();
    @Shadow public abstract int getHeight();

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void onIsActive(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof AbstractStringWidget textWidget && !((Object) this instanceof FocusableTextWidget)) {
            AccessibilityConfig config = AccessibilityConfig.get();
            if (config.isMasterEnabled() && config.isScreenNarrationEnabled()
                    && ScreenTextHelper.shouldFocusTextWidget(textWidget)) {
                cir.setReturnValue(textWidget.visible);
            }
        }
    }

    @Inject(method = "playDownSound", at = @At("HEAD"), cancellable = true)
    private void onPlayDownSound(SoundManager soundManager, CallbackInfo ci) {
        if ((Object) this instanceof AbstractStringWidget && !((Object) this instanceof FocusableTextWidget)) {
            ci.cancel();
        }
    }

    @Inject(method = "nextFocusPath", at = @At("HEAD"), cancellable = true)
    private void onNextFocusPath(FocusNavigationEvent event, CallbackInfoReturnable<ComponentPath> cir) {
        if ((Object) this instanceof AbstractStringWidget textWidget && !((Object) this instanceof FocusableTextWidget)) {
            AccessibilityConfig config = AccessibilityConfig.get();
            if (config.isMasterEnabled() && config.isScreenNarrationEnabled()
                    && ScreenTextHelper.shouldFocusTextWidget(textWidget)) {
                cir.setReturnValue(!this.isFocused() ? ComponentPath.leaf(textWidget) : null);
            }
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onExtractRenderStateStart(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ScreenTextHelper.pushWidgetRender((AbstractWidget) (Object) this);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void onExtractRenderStateEnd(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ScreenTextHelper.popWidgetRender((AbstractWidget) (Object) this);
        if ((Object) this instanceof AbstractStringWidget textWidget && !((Object) this instanceof FocusableTextWidget)) {
            if (textWidget.visible && this.isFocused()) {
                AccessibilityConfig config = AccessibilityConfig.get();
                if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
                    graphics.outline(this.getX() - 2, this.getY() - 2, this.getWidth() + 4, this.getHeight() + 4, 0xFFFFFFFF);
                }
            }
        }
    }
}
