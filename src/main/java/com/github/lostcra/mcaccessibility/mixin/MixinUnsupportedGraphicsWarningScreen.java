package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.navigation.ScreenTextHelper;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.UnsupportedGraphicsWarningScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(UnsupportedGraphicsWarningScreen.class)
public abstract class MixinUnsupportedGraphicsWarningScreen extends Screen {
    @Shadow @Final private List<Component> message;
    @Shadow private MultiLineLabel messageLines;
    @Shadow private int contentTop;

    protected MixinUnsupportedGraphicsWarningScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int h = Math.max(18, this.messageLines.getLineCount() * 9 + 4);
        ScreenTextHelper.addReadableText(
                this,
                25,
                Math.max(20, this.contentTop - 2),
                Math.max(100, this.width - 50),
                h,
                () -> CommonComponents.joinForNarration(this.message.toArray(Component[]::new))
        );
    }
}
