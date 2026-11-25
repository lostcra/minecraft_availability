package com.github.lostcra.mcaccessibility.navigation;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.mixin.AccessorAbstractContainerScreen;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.ref.WeakReference;
import java.util.List;

public class InventoryNavigator {
    private static int selectedIndex = 0;
    private static WeakReference<Slot> lastAnnouncedSlotRef = new WeakReference<>(null);

    public static void onScreenOpened(AbstractContainerScreen<?> screen) {
        lastAnnouncedSlotRef = new WeakReference<>(null);
        selectedIndex = 0;
        String screenTitle = screen.getTitle().getString();
        SpeechEngine.getInstance().speak("Контейнер: " + screenTitle, true);

        AccessorAbstractContainerScreen acc = (AccessorAbstractContainerScreen) screen;
        AbstractContainerMenu menu = acc.mcaccess$getMenu();
        if (menu != null && !menu.slots.isEmpty()) {
            Slot slot = menu.slots.get(0);
            acc.mcaccess$setHoveredSlot(slot);
            announceSlot(screen, slot, false);
        }
    }

    public static void onSlotHoverChanged(AbstractContainerScreen<?> screen, Slot newSlot) {
        if (!AccessibilityConfig.get().isInventoryNarrationEnabled()) return;
        if (newSlot != null && newSlot != lastAnnouncedSlotRef.get()) {
            lastAnnouncedSlotRef = new WeakReference<>(newSlot);
            AccessorAbstractContainerScreen acc = (AccessorAbstractContainerScreen) screen;
            AbstractContainerMenu menu = acc.mcaccess$getMenu();
            if (menu != null) {
                int idx = menu.slots.indexOf(newSlot);
                if (idx >= 0) {
                    selectedIndex = idx;
                }
            }
            announceSlot(screen, newSlot, true);
        }
    }

    public static void navigate(AbstractContainerScreen<?> screen, int dirX, int dirY) {
        AccessorAbstractContainerScreen acc = (AccessorAbstractContainerScreen) screen;
        AbstractContainerMenu menu = acc.mcaccess$getMenu();
        if (menu == null || menu.slots.isEmpty()) return;

        int total = menu.slots.size();
        int step = dirX;
        if (dirY != 0) {
            step = dirY * 9; // Jump 9 slots up/down for inventory rows
        }

        int targetIndex = selectedIndex + step;
        if (targetIndex < 0) {
            targetIndex = (targetIndex % total + total) % total;
        } else if (targetIndex >= total) {
            targetIndex = targetIndex % total;
        }

        selectedIndex = targetIndex;
        Slot targetSlot = menu.slots.get(selectedIndex);
        acc.mcaccess$setHoveredSlot(targetSlot);
        lastAnnouncedSlotRef = new WeakReference<>(targetSlot);
        announceSlot(screen, targetSlot, true);
    }

    public static void cycleSlot(AbstractContainerScreen<?> screen, boolean forward) {
        AccessorAbstractContainerScreen acc = (AccessorAbstractContainerScreen) screen;
        AbstractContainerMenu menu = acc.mcaccess$getMenu();
        if (menu == null || menu.slots.isEmpty()) return;

        int total = menu.slots.size();
        selectedIndex = forward ? (selectedIndex + 1) % total : (selectedIndex - 1 + total) % total;
        Slot targetSlot = menu.slots.get(selectedIndex);
        acc.mcaccess$setHoveredSlot(targetSlot);
        lastAnnouncedSlotRef = new WeakReference<>(targetSlot);
        announceSlot(screen, targetSlot, true);
    }

    public static void interact(AbstractContainerScreen<?> screen, boolean quickMove) {
        AccessorAbstractContainerScreen acc = (AccessorAbstractContainerScreen) screen;
        AbstractContainerMenu menu = acc.mcaccess$getMenu();
        if (menu == null || menu.slots.isEmpty()) return;

        if (selectedIndex >= 0 && selectedIndex < menu.slots.size()) {
            Slot slot = menu.slots.get(selectedIndex);
            ItemStack beforeStack = slot.getItem().copy();
            ItemStack carriedBefore = menu.getCarried().copy();

            ContainerInput inputType = quickMove ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP;
            acc.mcaccess$slotClicked(slot, slot.index, 0, inputType);

            // Announce resulting action
            ItemStack carriedAfter = menu.getCarried();
            ItemStack afterStack = slot.getItem();

            if (quickMove) {
                if (!beforeStack.isEmpty()) {
                    SpeechEngine.getInstance().speak("Быстро перемещено: " + beforeStack.getHoverName().getString(), true);
                }
            } else if (!carriedBefore.isEmpty() && carriedAfter.isEmpty()) {
                SpeechEngine.getInstance().speak("Помещено: " + carriedBefore.getHoverName().getString(), true);
            } else if (carriedBefore.isEmpty() && !carriedAfter.isEmpty()) {
                SpeechEngine.getInstance().speak("Взято: " + carriedAfter.getHoverName().getString() + " (" + carriedAfter.getCount() + " шт.)", true);
            } else {
                announceSlot(screen, slot, true);
            }
        }
    }

    public static void repeatCurrent(AbstractContainerScreen<?> screen) {
        AccessorAbstractContainerScreen acc = (AccessorAbstractContainerScreen) screen;
        AbstractContainerMenu menu = acc.mcaccess$getMenu();
        if (menu != null && selectedIndex >= 0 && selectedIndex < menu.slots.size()) {
            announceSlot(screen, menu.slots.get(selectedIndex), true);
        }
    }

    public static void announceSlot(AbstractContainerScreen<?> screen, Slot slot, boolean interrupt) {
        if (!AccessibilityConfig.get().isInventoryNarrationEnabled()) return;
        if (slot == null) return;

        AccessorAbstractContainerScreen acc = (AccessorAbstractContainerScreen) screen;
        AbstractContainerMenu menu = acc.mcaccess$getMenu();

        StringBuilder sb = new StringBuilder();
        int slotNumber = slot.getContainerSlot() + 1;
        sb.append("Слот ").append(slotNumber).append(". ");

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            sb.append(stack.getHoverName().getString());
            if (stack.getCount() > 1) {
                sb.append(", ").append(stack.getCount()).append(" шт.");
            }

            if (AccessibilityConfig.get().isDetailedTooltips()) {
                try {
                    List<Component> tooltip = acc.mcaccess$getTooltip(stack);
                    if (tooltip != null && tooltip.size() > 1) {
                        for (int i = 1; i < tooltip.size(); i++) {
                            String line = tooltip.get(i).getString().trim();
                            if (!line.isEmpty()) {
                                sb.append(". ").append(line);
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } else {
            sb.append("Пусто");
        }

        if (menu != null) {
            ItemStack carried = menu.getCarried();
            if (!carried.isEmpty()) {
                sb.append(" (В руке: ").append(carried.getHoverName().getString())
                  .append(", ").append(carried.getCount()).append(" шт.)");
            }
        }

        SpeechEngine.getInstance().speak(sb.toString(), interrupt);
    }
}
