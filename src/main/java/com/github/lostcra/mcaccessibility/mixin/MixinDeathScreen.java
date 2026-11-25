package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.navigation.ScreenTextHelper;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class MixinDeathScreen extends Screen {
    @Shadow @Final private Component causeOfDeath;
    @Shadow @Final private Component deathScore;

    protected MixinDeathScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int cx = this.width / 2;
        ScreenTextHelper.addReadableText(this, cx - 155, 80, 310, 40, () -> {
            if (this.causeOfDeath != null && this.deathScore != null) {
                return CommonComponents.joinForNarration(this.causeOfDeath, this.deathScore);
            }
            return this.deathScore != null ? this.deathScore : (this.causeOfDeath != null ? this.causeOfDeath : this.getTitle());
        });
    }
}
