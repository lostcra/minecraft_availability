package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.navigation.EditBoxNarrationHelper;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.GameNarrator;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameNarrator.class)
public class MixinGameNarrator {

    @Inject(method = "saySystemNow(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onSaySystemNow(String text, CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            if (text != null && !text.trim().isEmpty()) {
                if (EditBoxNarrationHelper.shouldSuppressScreenNarration(text.trim())) {
                    ci.cancel();
                    return;
                }
                SpeechEngine.getInstance().speak(text.trim(), true);
            }
            ci.cancel();
        }
    }

    @Inject(method = "saySystemQueued(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void onSaySystemQueued(Component message, CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            if (message != null) {
                String text = message.getString().trim();
                if (!text.isEmpty()) {
                    SpeechEngine.getInstance().speak(text, false);
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "sayChatQueued", at = @At("HEAD"), cancellable = true)
    private void onSayChatQueued(Component message, CallbackInfo ci) {
        if (AccessibilityConfig.get().isMasterEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "saySystemChatQueued", at = @At("HEAD"), cancellable = true)
    private void onSaySystemChatQueued(Component message, CallbackInfo ci) {
        if (AccessibilityConfig.get().isMasterEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void onIsActive(CallbackInfoReturnable<Boolean> cir) {
        if (AccessibilityConfig.get().isMasterEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "clear", at = @At("HEAD"), cancellable = true)
    private void onClear(CallbackInfo ci) {
        if (AccessibilityConfig.get().isMasterEnabled()) {
            SpeechEngine.getInstance().stop();
            ci.cancel();
        }
    }
}
