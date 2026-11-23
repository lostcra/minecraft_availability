package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.navigation.F3NarrationHelper;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenEntryList.class)
public class MixinDebugScreenEntryList {

    @Inject(method = "setOverlayVisible", at = @At("RETURN"))
    private void onSetOverlayVisible(boolean visible, CallbackInfo ci) {
        F3NarrationHelper.onOverlayVisibilityChanged(visible);
    }
}
