package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.navigation.AdvancementNavigator;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementsScreen.class)
public class MixinAdvancementsScreen {

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isAdvancementNarrationEnabled()) {
            AdvancementNavigator.onScreenOpened((AdvancementsScreen) (Object) this);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isKeyboardAdvancementsNav()) {
            return;
        }

        AdvancementsScreen screen = (AdvancementsScreen) (Object) this;

        if (event.isCycleFocus() || event.key() == InputConstants.KEY_TAB) {
            AdvancementNavigator.cycleTab(screen, !event.hasShiftDown());
            cir.setReturnValue(true);
            return;
        }

        if (event.isRight() || event.key() == InputConstants.KEY_RIGHT) {
            AdvancementNavigator.navigate(screen, 1, 0);
            cir.setReturnValue(true);
            return;
        }

        if (event.isLeft() || event.key() == InputConstants.KEY_LEFT) {
            AdvancementNavigator.navigate(screen, -1, 0);
            cir.setReturnValue(true);
            return;
        }

        if (event.isDown() || event.key() == InputConstants.KEY_DOWN) {
            AdvancementNavigator.navigate(screen, 0, 1);
            cir.setReturnValue(true);
            return;
        }

        if (event.isUp() || event.key() == InputConstants.KEY_UP) {
            AdvancementNavigator.navigate(screen, 0, -1);
            cir.setReturnValue(true);
            return;
        }

        if (event.isConfirmation() || event.key() == InputConstants.KEY_SPACE || event.key() == InputConstants.KEY_RETURN) {
            AdvancementNavigator.repeatCurrent();
            cir.setReturnValue(true);
        }
    }
}
