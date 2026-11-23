package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class MixinChatComponent {

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD")
    )
    private void onAddMessage(Component message, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isChatNarrationEnabled() && message != null) {
            String text = message.getString().trim();
            if (!text.isEmpty()) {
                SpeechEngine.getInstance().speak(text, false);
            }
        }
    }
}
