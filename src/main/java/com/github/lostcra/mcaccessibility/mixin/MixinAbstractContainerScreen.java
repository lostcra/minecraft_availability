package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.navigation.InventoryNavigator;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {
    @Shadow protected Slot hoveredSlot;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isInventoryNarrationEnabled()) {
            InventoryNavigator.onScreenOpened((AbstractContainerScreen<?>) (Object) this);
        }
    }

    @Inject(method = "containerTick", at = @At("HEAD"))
    private void onContainerTick(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isInventoryNarrationEnabled()) {
            InventoryNavigator.onSlotHoverChanged((AbstractContainerScreen<?>) (Object) this, hoveredSlot);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isKeyboardInventoryNav()) {
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        if (event.isLeft() || event.key() == InputConstants.KEY_LEFT) {
            InventoryNavigator.navigate(screen, -1, 0);
            cir.setReturnValue(true);
            return;
        }

        if (event.isRight() || event.key() == InputConstants.KEY_RIGHT) {
            InventoryNavigator.navigate(screen, 1, 0);
            cir.setReturnValue(true);
            return;
        }

        if (event.isUp() || event.key() == InputConstants.KEY_UP) {
            InventoryNavigator.navigate(screen, 0, -1);
            cir.setReturnValue(true);
            return;
        }

        if (event.isDown() || event.key() == InputConstants.KEY_DOWN) {
            InventoryNavigator.navigate(screen, 0, 1);
            cir.setReturnValue(true);
            return;
        }

        if (event.isCycleFocus() || event.key() == InputConstants.KEY_TAB) {
            InventoryNavigator.cycleSlot(screen, !event.hasShiftDown());
            cir.setReturnValue(true);
            return;
        }

        if (event.isConfirmation() || event.key() == InputConstants.KEY_SPACE || event.key() == InputConstants.KEY_RETURN) {
            InventoryNavigator.interact(screen, event.hasShiftDown());
            cir.setReturnValue(true);
            return;
        }

        if (event.key() == InputConstants.KEY_R || event.keycode() == InputConstants.KEYCODE_R) {
            InventoryNavigator.repeatCurrent(screen);
            cir.setReturnValue(true);
        }
    }
}
