package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.navigation.AdvancementNavigator;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class MixinToastManager {
    @Inject(method = "addToast", at = @At("HEAD"))
    private void onAddToast(Toast toast, CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isAdvancementNarrationEnabled()) {
            return;
        }
        if (toast instanceof AdvancementToast advToast) {
            AdvancementHolder holder = ((AccessorAdvancementToast) advToast).mcaccess$getAdvancement();
            AdvancementNavigator.announceCompleted(holder);
        }
    }
}
