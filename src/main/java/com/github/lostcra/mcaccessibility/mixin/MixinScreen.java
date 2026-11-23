package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.navigation.EditBoxNarrationHelper;
import net.minecraft.client.gui.narration.NarrationTrigger;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Shadow public abstract Component getTitle();
    @Shadow private long narrationSuppressTime;

    @Shadow private void scheduleNarration(long delay, boolean suppressPrevious, NarrationTrigger trigger) {}

    @Inject(method = "shouldRunNarration", at = @At("HEAD"), cancellable = true)
    private void onShouldRunNarration(CallbackInfoReturnable<Boolean> cir) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "afterKeyboardAction", at = @At("HEAD"), cancellable = true)
    private void onAfterKeyboardAction(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            if (EditBoxNarrationHelper.consumeSuppressScreenNarration()) {
                ci.cancel();
                return;
            }
            this.scheduleNarration(100L, true, NarrationTrigger.KEYBOARD);
            ci.cancel();
        }
    }

    @Inject(method = "afterMouseMove", at = @At("HEAD"), cancellable = true)
    private void onAfterMouseMove(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            if (EditBoxNarrationHelper.consumeSuppressScreenNarration()) {
                ci.cancel();
                return;
            }
            this.scheduleNarration(250L, true, NarrationTrigger.MOUSE);
            ci.cancel();
        }
    }

    @Inject(method = "afterMouseAction", at = @At("HEAD"), cancellable = true)
    private void onAfterMouseAction(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            if (EditBoxNarrationHelper.consumeSuppressScreenNarration()) {
                ci.cancel();
                return;
            }
            this.scheduleNarration(100L, true, NarrationTrigger.MOUSE);
            ci.cancel();
        }
    }

    @Inject(method = "scheduleNarration(JZLnet/minecraft/client/gui/narration/NarrationTrigger;)V", at = @At("HEAD"))
    private void onScheduleNarration(long delay, boolean suppressPrevious, NarrationTrigger trigger, CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            this.narrationSuppressTime = Long.MIN_VALUE;
        }
    }

    @Inject(method = "getNarrationMessage", at = @At("RETURN"), cancellable = true)
    private void onGetNarrationMessage(CallbackInfoReturnable<Component> cir) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        Component original = cir.getReturnValue();
        if (original != null) {
            String str = original.getString().trim();
            if (!str.isEmpty() && !str.startsWith("Экран") && !str.startsWith("Screen")) {
                cir.setReturnValue(Component.literal("Экран: " + str));
            }
        }
    }

    @Inject(method = "triggerImmediateNarration", at = @At("HEAD"), cancellable = true)
    private void onTriggerImmediateNarration(boolean interrupt, CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        if ((Object) this instanceof AbstractContainerScreen && config.isInventoryNarrationEnabled()) {
            ci.cancel();
            return;
        }
        if ((Object) this instanceof AdvancementsScreen && config.isAdvancementNarrationEnabled()) {
            ci.cancel();
            return;
        }
    }
}
