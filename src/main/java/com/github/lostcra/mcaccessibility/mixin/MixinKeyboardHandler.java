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
        if (action == 1) {
            int key = event.key();
            int code = event.keycode();
            AccessibilityConfig config = AccessibilityConfig.get();

            // 1. Mod hotkeys (Ctrl + Alt + A/C/S/F)
            if (event.hasControlDown() && event.hasAltDown()) {
                if (key == InputConstants.KEY_A || code == InputConstants.KEYCODE_A) {
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

                if (key == InputConstants.KEY_S || code == 31) {
                    minecraft.execute(() -> minecraft.setScreenAndShow(AccessibilityConfigScreen.create(minecraft.gui.screen())));
                    ci.cancel();
                    return;
                }

                if (key == InputConstants.KEY_F || code == InputConstants.KEYCODE_F) {
                    if (minecraft.player != null) {
                        com.github.lostcra.mcaccessibility.navigation.F3NarrationHelper.narrateF3Summary(minecraft);
                        ci.cancel();
                        return;
                    }
                }

                if (key == InputConstants.KEY_R || code == InputConstants.KEYCODE_R) {
                    if (minecraft.gui != null && minecraft.gui.screen() != null) {
                        com.github.lostcra.mcaccessibility.navigation.ScreenTextHelper.requestReadCurrentScreen(minecraft);
                        ci.cancel();
                        return;
                    }
                }
            }

            // 2. Interrupt ongoing speech on key press:
            // Standard screen reader silence: pressing Control key alone stops current speech!
            // Or if interruptOnKeyPress is enabled, ANY key press stops current speech.
            boolean isControlOnly = (key == InputConstants.KEY_LCONTROL || key == InputConstants.KEY_RCONTROL
                    || key == 341 || key == 345);
            if (config.isMasterEnabled() && (isControlOnly || config.isInterruptOnKeyPress())) {
                SpeechEngine.getInstance().stop();
            }
        }
    }
}
