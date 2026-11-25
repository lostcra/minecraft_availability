package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.navigation.ScreenTextHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.worldupdate.WorldUpgrader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptimizeWorldScreen.class)
public abstract class MixinOptimizeWorldScreen extends Screen {
    @Shadow @Final private WorldUpgrader upgrader;

    protected MixinOptimizeWorldScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int cx = this.width / 2;
        ScreenTextHelper.addReadableText(this, cx - 150, 38, 300, 70, () -> {
            if (this.upgrader == null) {
                return this.getTitle();
            }
            String status = this.upgrader.getStatus() != null ? this.upgrader.getStatus().getString() : "";
            int total = this.upgrader.getTotalChunks();
            if (total > 0) {
                int converted = this.upgrader.getConverted();
                int skipped = this.upgrader.getSkipped();
                int pct = Math.round(this.upgrader.getTotalProgress() * 100.0f);
                return Component.literal(status + ". " + pct + "%. Чанков обработано: "
                        + (converted + skipped) + " из " + total
                        + " (конвертировано: " + converted + ", пропущено: " + skipped + ")");
            }
            return Component.literal(status);
        });
    }
}
