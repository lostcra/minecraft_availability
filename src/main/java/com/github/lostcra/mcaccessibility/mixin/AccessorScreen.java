package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface AccessorScreen {
    @Accessor("initialized")
    boolean mcaccess$isInitialized();

    @Accessor("children")
    List<GuiEventListener> mcaccess$getChildren();

    @Accessor("narratables")
    List<NarratableEntry> mcaccess$getNarratables();

    @Accessor("renderables")
    List<Renderable> mcaccess$getRenderables();
}
