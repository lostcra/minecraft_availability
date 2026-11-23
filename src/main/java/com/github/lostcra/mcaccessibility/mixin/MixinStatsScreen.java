package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.navigation.StatsNarrationHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatsScreen.class)
public abstract class MixinStatsScreen extends Screen {
    protected MixinStatsScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        StatsNarrationHelper.onScreenOpened((StatsScreen) (Object) this);
    }

    @Inject(method = "onStatsUpdated", at = @At("RETURN"))
    private void onStatsUpdated(CallbackInfo ci) {
        StatsNarrationHelper.selectFirstEntry((StatsScreen) (Object) this, false);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }

        int key = event.key();
        StatsScreen screen = (StatsScreen) (Object) this;

        if (key == InputConstants.KEY_UP) {
            StatsNarrationHelper.navigate(screen, -1);
            cir.setReturnValue(true);
        } else if (key == InputConstants.KEY_DOWN) {
            StatsNarrationHelper.navigate(screen, 1);
            cir.setReturnValue(true);
        } else if (key == InputConstants.KEY_PAGEUP || (event.hasControlDown() && key == InputConstants.KEY_LEFT)) {
            StatsNarrationHelper.cycleTab(screen, false);
            cir.setReturnValue(true);
        } else if (key == InputConstants.KEY_PAGEDOWN || (event.hasControlDown() && key == InputConstants.KEY_RIGHT)) {
            StatsNarrationHelper.cycleTab(screen, true);
            cir.setReturnValue(true);
        } else if (key == InputConstants.KEY_HOME) {
            StatsNarrationHelper.navigateHome(screen);
            cir.setReturnValue(true);
        } else if (key == InputConstants.KEY_END) {
            StatsNarrationHelper.navigateEnd(screen);
            cir.setReturnValue(true);
        } else if (key == InputConstants.KEY_SPACE || key == InputConstants.KEY_RETURN) {
            StatsNarrationHelper.repeatSelected(screen);
            cir.setReturnValue(true);
        }
    }
}
