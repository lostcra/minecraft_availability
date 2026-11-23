package com.github.lostcra.mcaccessibility.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public interface AccessorAbstractContainerScreen {
    @Accessor("hoveredSlot")
    Slot mcaccess$getHoveredSlot();

    @Accessor("hoveredSlot")
    void mcaccess$setHoveredSlot(Slot slot);

    @Accessor("menu")
    AbstractContainerMenu mcaccess$getMenu();

    @Invoker("slotClicked")
    void mcaccess$slotClicked(Slot slot, int slotId, int mouseButton, ContainerInput type);

    @Invoker("getTooltipFromContainerItem")
    List<Component> mcaccess$getTooltip(ItemStack stack);
}
