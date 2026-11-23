package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screens.debug.DebugOptionsScreen$OptionEntry")
public class MixinDebugOptionsEntry {
    @Shadow @Final private String name;

    @Inject(method = "narrateButton", at = @At("RETURN"), cancellable = true)
    private void onNarrateButton(CycleButton<Boolean> button, CallbackInfoReturnable<MutableComponent> cir) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && (config.isScreenNarrationEnabled() || config.isF3NarrationEnabled())) {
            MutableComponent original = cir.getReturnValue();
            if (this.name != null) {
                cir.setReturnValue(Component.literal(this.name + ": ").append(original));
            }
        }
    }
}
