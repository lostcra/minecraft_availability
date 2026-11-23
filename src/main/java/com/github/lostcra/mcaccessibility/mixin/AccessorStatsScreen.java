package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.stats.StatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StatsScreen.class)
public interface AccessorStatsScreen {
    @Accessor("stats")
    StatsCounter mcaccess$getStats();

    @Accessor("tabManager")
    TabManager mcaccess$getTabManager();

    @Accessor("tabNavigationBar")
    MenuTabBar mcaccess$getTabBar();

    @Accessor("isLoading")
    boolean mcaccess$isLoading();
}
