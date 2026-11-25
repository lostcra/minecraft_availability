package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementToast.class)
public interface AccessorAdvancementToast {
    @Accessor("advancement")
    AdvancementHolder mcaccess$getAdvancement();
}
