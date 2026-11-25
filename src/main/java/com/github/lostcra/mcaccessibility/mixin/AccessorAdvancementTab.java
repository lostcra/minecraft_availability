package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.advancements.AdvancementHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementTab.class)
public interface AccessorAdvancementTab {
    @Accessor("widgets")
    Map<AdvancementHolder, AdvancementWidget> mcaccess$getWidgets();

    @Accessor("root")
    AdvancementWidget mcaccess$getRoot();

    @Accessor("hovered")
    AdvancementWidget mcaccess$getHovered();
}
