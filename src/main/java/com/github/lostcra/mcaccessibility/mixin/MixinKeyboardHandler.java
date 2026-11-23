package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.config.AccessibilityConfigScreen;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        // action 1 = Press
        if (action == 1 && event.hasControlDown() && event.hasAltDown()) {
            int key = event.key();
            int code = event.keycode();
            AccessibilityConfig config = AccessibilityConfig.get();

            if (key == InputConstants.KEY_A || code == InputConstants.KEYCODE_A) {
                // Ctrl + Alt + A: Global voiceover toggle
                boolean newState = !config.isMasterEnabled();
                config.setMasterEnabled(newState);
                config.save();
                if (config.isNarrateHotkeys()) {
                    String msg = newState ? "Озвучка игры включена. Accessibility voiceover enabled." 
                                          : "Озвучка игры отключена. Accessibility voiceover disabled.";
                    SpeechEngine.getInstance().speak(msg, true);
                }
                ci.cancel();
                return;
            }

            if (key == InputConstants.KEY_C || code == InputConstants.KEYCODE_C) {
                // Ctrl + Alt + C: Chat narration toggle
                boolean newState = !config.isChatNarrationEnabled();
                config.setChatNarrationEnabled(newState);
                config.save();
                if (config.isNarrateHotkeys()) {
                    String msg = newState ? "Озвучивание чата включено. Chat narration enabled." 
                                          : "Озвучивание чата отключено. Chat narration disabled.";
                    SpeechEngine.getInstance().speak(msg, true);
                }
                ci.cancel();
                return;
            }

            if (key == InputConstants.KEY_S) {
                // Ctrl + Alt + S: Open Accessibility Settings
                minecraft.setScreenAndShow(AccessibilityConfigScreen.create(minecraft.gui.screen()));
                ci.cancel();
                return;
            }
        }
    }
}
