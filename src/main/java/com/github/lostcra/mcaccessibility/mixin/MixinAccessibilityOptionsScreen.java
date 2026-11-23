package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilitySettingsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AccessibilityOptionsScreen.class)
public abstract class MixinAccessibilityOptionsScreen extends Screen {
    protected MixinAccessibilityOptionsScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int btnWidth = 160;
        int btnHeight = 20;
        int x = this.width - btnWidth - 8;
        int y = 6;

        Button modSettingsBtn = addRenderableWidget(Button.builder(
                Component.literal("Синтезатор и обучение..."),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new AccessibilitySettingsScreen(this));
                    }
                }
        ).bounds(x, y, btnWidth, btnHeight).build());
        modSettingsBtn.setTooltip(Tooltip.create(Component.literal("Настройки синтезатора речи, выбор голоса и руководство (быстрый старт). Горячая клавиша: Ctrl+Alt+S")));
    }
}
