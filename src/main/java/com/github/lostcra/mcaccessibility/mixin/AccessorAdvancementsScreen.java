package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.advancements.AdvancementHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public interface AccessorAdvancementsScreen {
    @Accessor("tabs")
    Map<AdvancementHolder, AdvancementTab> mcaccess$getTabs();

    @Accessor("selectedTab")
    AdvancementTab mcaccess$getSelectedTab();

    @Accessor("selectedTab")
    void mcaccess$setSelectedTab(AdvancementTab tab);
}
