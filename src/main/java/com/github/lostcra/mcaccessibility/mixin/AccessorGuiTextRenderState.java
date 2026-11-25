package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiTextRenderState.class)
public interface AccessorGuiTextRenderState {
    @Accessor("text")
    FormattedCharSequence mcaccess$getText();

    @Accessor("x")
    int mcaccess$getX();

    @Accessor("y")
    int mcaccess$getY();
}
