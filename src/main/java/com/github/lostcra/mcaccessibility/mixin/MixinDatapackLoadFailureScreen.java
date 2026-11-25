package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.navigation.ScreenTextHelper;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DatapackLoadFailureScreen.class)
public abstract class MixinDatapackLoadFailureScreen extends Screen {
    @Shadow private MultiLineLabel message;

    protected MixinDatapackLoadFailureScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int h = Math.max(18, this.message.getLineCount() * 9 + 4);
        ScreenTextHelper.addReadableText(this, 25, 78, Math.max(100, this.width - 50), h, this::getTitle);
    }
}
