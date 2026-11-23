package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screens.achievement.StatsScreen$ItemStatisticsList$ItemRow")
public interface AccessorItemRow {
    @Accessor("item")
    Item mcaccess$getItem();
}
