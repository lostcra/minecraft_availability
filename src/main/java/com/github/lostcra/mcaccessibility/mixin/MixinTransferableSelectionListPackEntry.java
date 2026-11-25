package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(TransferableSelectionList.PackEntry.class)
public abstract class MixinTransferableSelectionListPackEntry {
    @Shadow @Final private PackSelectionModel.Entry pack;

    @Inject(method = "getNarration", at = @At("RETURN"), cancellable = true)
    private void onGetNarration(CallbackInfoReturnable<Component> cir) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled() || this.pack == null) {
            return;
        }

        List<Component> parts = new ArrayList<>();
        Component title = this.pack.getTitle();
        if (title != null && !title.getString().trim().isEmpty()) {
            parts.add(title);
        }

        Component description = this.pack.getExtendedDescription();
        if (description == null || description.getString().trim().isEmpty()) {
            description = this.pack.getDescription();
        }
        if (description != null && !description.getString().trim().isEmpty()) {
            parts.add(description);
        }

        if (this.pack.getCompatibility() != null && !this.pack.getCompatibility().isCompatible()) {
            Component compatDesc = this.pack.getCompatibility().getDescription();
            if (compatDesc != null && !compatDesc.getString().trim().isEmpty()) {
                parts.add(compatDesc);
            }
        }

        if (!parts.isEmpty()) {
            cir.setReturnValue(Component.translatable(
                    "narrator.select",
                    CommonComponents.joinForNarration(parts.toArray(Component[]::new))
            ));
        }
    }
}
