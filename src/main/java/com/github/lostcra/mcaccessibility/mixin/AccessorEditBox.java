package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditBox.class)
public interface AccessorEditBox {
    @Accessor("hint")
    Component mcaccess$getHint();

    @Accessor("highlightPos")
    int mcaccess$getHighlightPos();

    @Accessor("canLoseFocus")
    boolean mcaccess$canLoseFocus();
}
