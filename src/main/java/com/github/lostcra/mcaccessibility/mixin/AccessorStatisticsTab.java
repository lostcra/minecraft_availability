package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screens.achievement.StatsScreen$StatisticsTab")
public interface AccessorStatisticsTab {
    @Accessor("list")
    AbstractSelectionList<?> mcaccess$getList();
}
