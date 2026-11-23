package com.github.lostcra.mcaccessibility.navigation;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.mixin.AccessorItemRow;
import com.github.lostcra.mcaccessibility.mixin.AccessorStatisticsTab;
import com.github.lostcra.mcaccessibility.mixin.AccessorStatsScreen;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.List;

public class StatsNarrationHelper {

    public static void onScreenOpened(StatsScreen screen) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        String title = screen.getTitle() != null ? screen.getTitle().getString() : "Статистика";
        SpeechEngine.getInstance().speak(title + ". Навигация по вкладкам: Page Up и Page Down. Навигация по списку: стрелки вверх и вниз.", true);
        selectFirstEntry(screen, false);
    }

    public static void cycleTab(StatsScreen screen, boolean forward) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }

        AccessorStatsScreen acc = (AccessorStatsScreen) screen;
        MenuTabBar bar = acc.mcaccess$getTabBar();
        if (bar == null) return;

        List<Tab> tabs = bar.getTabs();
        if (tabs == null || tabs.isEmpty()) return;

        Tab currentTab = acc.mcaccess$getTabManager().getCurrentTab();
        int curIdx = tabs.indexOf(currentTab);
        if (curIdx < 0) curIdx = 0;

        int nextIdx = forward ? (curIdx + 1) % tabs.size() : (curIdx - 1 + tabs.size()) % tabs.size();
        bar.selectTab(nextIdx, true);

        Tab nextTab = tabs.get(nextIdx);
        String tabName = nextTab.getTabTitle().getString();
        SpeechEngine.getInstance().speak("Вкладка: " + tabName, true);

        selectFirstEntry(screen, true);
    }

    public static void selectFirstEntry(StatsScreen screen, boolean speak) {
        AbstractSelectionList<?> list = getCurrentList(screen);
        if (list == null || list.children().isEmpty()) return;

        Object first = list.children().get(0);
        setSelectedEntry(list, first);
        if (speak) {
            announceEntry(screen, first, false);
        }
    }

    public static void navigate(StatsScreen screen, int delta) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }

        AbstractSelectionList<?> list = getCurrentList(screen);
        if (list == null || list.children().isEmpty()) return;

        List<?> items = list.children();
        Object current = list.getSelected();
        int curIdx = current != null ? items.indexOf(current) : -1;

        int nextIdx = curIdx + delta;
        if (nextIdx < 0) {
            nextIdx = 0;
            SpeechEngine.getInstance().speak("Начало списка", true);
            return;
        } else if (nextIdx >= items.size()) {
            nextIdx = items.size() - 1;
            SpeechEngine.getInstance().speak("Конец списка", true);
            return;
        }

        Object nextItem = items.get(nextIdx);
        setSelectedEntry(list, nextItem);
        announceEntry(screen, nextItem, true);
    }

    public static void navigateHome(StatsScreen screen) {
        AbstractSelectionList<?> list = getCurrentList(screen);
        if (list == null || list.children().isEmpty()) return;
        Object first = list.children().get(0);
        setSelectedEntry(list, first);
        announceEntry(screen, first, true);
    }

    public static void navigateEnd(StatsScreen screen) {
        AbstractSelectionList<?> list = getCurrentList(screen);
        if (list == null || list.children().isEmpty()) return;
        Object last = list.children().get(list.children().size() - 1);
        setSelectedEntry(list, last);
        announceEntry(screen, last, true);
    }

    public static void repeatSelected(StatsScreen screen) {
        AbstractSelectionList<?> list = getCurrentList(screen);
        if (list == null) return;
        Object current = list.getSelected();
        if (current != null) {
            announceEntry(screen, current, true);
        } else if (!list.children().isEmpty()) {
            selectFirstEntry(screen, true);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setSelectedEntry(AbstractSelectionList list, Object entry) {
        try {
            for (java.lang.reflect.Method m : AbstractSelectionList.class.getDeclaredMethods()) {
                if (m.getName().equals("setSelected") && m.getParameterCount() == 1) {
                    m.setAccessible(true);
                    m.invoke(list, entry);
                    break;
                }
            }
        } catch (Throwable ignored) {}
    }

    public static AbstractSelectionList<?> getCurrentList(StatsScreen screen) {
        try {
            AccessorStatsScreen acc = (AccessorStatsScreen) screen;
            Tab cur = acc.mcaccess$getTabManager().getCurrentTab();
            if (cur instanceof AccessorStatisticsTab statTab) {
                return statTab.mcaccess$getList();
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static void announceEntry(StatsScreen screen, Object entry, boolean interrupt) {
        if (entry == null) return;

        AccessorStatsScreen acc = (AccessorStatsScreen) screen;
        StatsCounter stats = acc.mcaccess$getStats();

        // 1. Item Row
        if (entry instanceof AccessorItemRow row) {
            Item item = row.mcaccess$getItem();
            if (item != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(item.getDefaultInstance().getHoverName().getString()).append(". ");

                boolean hasAction = false;
                if (stats != null) {
                    if (item instanceof BlockItem bi) {
                        int mined = stats.getValue(Stats.BLOCK_MINED.get(bi.getBlock()));
                        if (mined > 0) {
                            sb.append(Component.translatable("stat_type.minecraft.mined").getString())
                              .append(": ").append(mined).append(". ");
                            hasAction = true;
                        }
                    }

                    int crafted = stats.getValue(Stats.ITEM_CRAFTED.get(item));
                    if (crafted > 0) {
                        sb.append(Component.translatable("stat_type.minecraft.crafted").getString())
                          .append(": ").append(crafted).append(". ");
                        hasAction = true;
                    }

                    int used = stats.getValue(Stats.ITEM_USED.get(item));
                    if (used > 0) {
                        sb.append(Component.translatable("stat_type.minecraft.used").getString())
                          .append(": ").append(used).append(". ");
                        hasAction = true;
                    }

                    int broken = stats.getValue(Stats.ITEM_BROKEN.get(item));
                    if (broken > 0) {
                        sb.append(Component.translatable("stat_type.minecraft.broken").getString())
                          .append(": ").append(broken).append(". ");
                        hasAction = true;
                    }

                    int pickedUp = stats.getValue(Stats.ITEM_PICKED_UP.get(item));
                    if (pickedUp > 0) {
                        sb.append(Component.translatable("stat_type.minecraft.picked_up").getString())
                          .append(": ").append(pickedUp).append(". ");
                        hasAction = true;
                    }

                    int dropped = stats.getValue(Stats.ITEM_DROPPED.get(item));
                    if (dropped > 0) {
                        sb.append(Component.translatable("stat_type.minecraft.dropped").getString())
                          .append(": ").append(dropped).append(". ");
                        hasAction = true;
                    }
                }

                if (!hasAction) {
                    sb.append(Component.translatable("mcaccessibility.stats.no_entries").getString());
                }

                SpeechEngine.getInstance().speak(sb.toString().trim(), interrupt);
                return;
            }
        }

        // 2. Mob Row or General Stat Entry via getNarration()
        try {
            java.lang.reflect.Method m = entry.getClass().getMethod("getNarration");
            Object res = m.invoke(entry);
            if (res instanceof Component comp) {
                String str = comp.getString().trim();
                if (!str.isEmpty()) {
                    SpeechEngine.getInstance().speak(str, interrupt);
                    return;
                }
            }
        } catch (Throwable ignored) {}

        // Fallback
        SpeechEngine.getInstance().speak(entry.toString(), interrupt);
    }
}
