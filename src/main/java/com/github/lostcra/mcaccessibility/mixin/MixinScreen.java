package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Shadow public abstract Component getTitle();
    @Shadow public abstract Component getNarrationMessage();

    private String lastNarratedWidgetMessage = "";

    @Inject(method = "added", at = @At("RETURN"))
    private void onScreenAdded(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        Component title = getTitle();
        if (title != null) {
            String titleStr = title.getString().trim();
            if (!titleStr.isEmpty()) {
                SpeechEngine.getInstance().speak("Экран: " + titleStr, true);
            }
        }
    }

    @Inject(method = "triggerImmediateNarration", at = @At("HEAD"))
    private void onTriggerImmediateNarration(boolean interrupt, CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        Component msg = getNarrationMessage();
        if (msg != null) {
            String msgStr = msg.getString().trim();
            if (!msgStr.isEmpty() && !msgStr.equals(lastNarratedWidgetMessage)) {
                lastNarratedWidgetMessage = msgStr;
                SpeechEngine.getInstance().speak(msgStr, interrupt);
            }
        }
    }
}
