package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.navigation.ScreenTextHelper;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public class MixinGuiRenderState {
    @Inject(method = "addText", at = @At("HEAD"))
    private void onAddText(GuiTextRenderState state, CallbackInfo ci) {
        if (ScreenTextHelper.isCapturingFrame()) {
            ScreenTextHelper.recordGuiText(state);
        }
    }
}
