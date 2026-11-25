package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AdvancementWidget.class)
public interface AccessorAdvancementWidget {
    @Accessor("progress")
    AdvancementProgress mcaccess$getProgress();

    @Accessor("parent")
    AdvancementWidget mcaccess$getParent();

    @Accessor("children")
    List<AdvancementWidget> mcaccess$getChildren();

    @Accessor("description")
    List<FormattedCharSequence> mcaccess$getDescriptionLines();
}
