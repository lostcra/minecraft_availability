package com.github.lostcra.mcaccessibility.navigation;

import com.github.lostcra.mcaccessibility.mixin.AccessorAdvancementTab;
import com.github.lostcra.mcaccessibility.mixin.AccessorAdvancementWidget;
import com.github.lostcra.mcaccessibility.mixin.AccessorAdvancementsScreen;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdvancementNavigator {
    private static WeakReference<AdvancementWidget> selectedWidgetRef = new WeakReference<>(null);
    private static WeakReference<AdvancementTab> lastTabRef = new WeakReference<>(null);

    public static void onScreenOpened(AdvancementsScreen screen) {
        AccessorAdvancementsScreen acc = (AccessorAdvancementsScreen) screen;
        AdvancementTab tab = acc.mcaccess$getSelectedTab();
        if (tab == null) {
            Collection<AdvancementTab> tabs = acc.mcaccess$getTabs().values();
            if (!tabs.isEmpty()) {
                tab = tabs.iterator().next();
                acc.mcaccess$setSelectedTab(tab);
            }
        }
        lastTabRef = new WeakReference<>(tab);
        if (tab != null) {
            String tabName = tab.getTitle().getString();
            SpeechEngine.getInstance().speak("Экран достижений. Вкладка: " + tabName, true);
            AccessorAdvancementTab tabAcc = (AccessorAdvancementTab) tab;
            AdvancementWidget root = tabAcc.mcaccess$getRoot();
            selectedWidgetRef = new WeakReference<>(root);
            if (root != null) {
                announceWidget(root, false);
            }
        } else {
            selectedWidgetRef = new WeakReference<>(null);
            SpeechEngine.getInstance().speak("Экран достижений. Достижения отсутствуют.", true);
        }
    }

    public static void cycleTab(AdvancementsScreen screen, boolean forward) {
        AccessorAdvancementsScreen acc = (AccessorAdvancementsScreen) screen;
        List<AdvancementTab> tabList = new ArrayList<>(acc.mcaccess$getTabs().values());
        if (tabList.isEmpty()) {
            return;
        }

        AdvancementTab cur = acc.mcaccess$getSelectedTab();
        int curIdx = tabList.indexOf(cur);
        if (curIdx < 0) curIdx = 0;

        int nextIdx = forward ? (curIdx + 1) % tabList.size() : (curIdx - 1 + tabList.size()) % tabList.size();
        AdvancementTab nextTab = tabList.get(nextIdx);
        acc.mcaccess$setSelectedTab(nextTab);
        lastTabRef = new WeakReference<>(nextTab);

        String tabName = nextTab.getTitle().getString();
        SpeechEngine.getInstance().speak("Вкладка: " + tabName, true);

        AccessorAdvancementTab tabAcc = (AccessorAdvancementTab) nextTab;
        AdvancementWidget root = tabAcc.mcaccess$getRoot();
        selectedWidgetRef = new WeakReference<>(root);
        if (root != null) {
            announceWidget(root, false);
        }
    }

    public static void navigate(AdvancementsScreen screen, int dirX, int dirY) {
        AccessorAdvancementsScreen acc = (AccessorAdvancementsScreen) screen;
        AdvancementTab tab = acc.mcaccess$getSelectedTab();
        if (tab == null) return;

        if (tab != lastTabRef.get()) {
            lastTabRef = new WeakReference<>(tab);
            selectedWidgetRef = new WeakReference<>(null);
        }

        AccessorAdvancementTab tabAcc = (AccessorAdvancementTab) tab;
        AdvancementWidget selectedWidget = selectedWidgetRef.get();
        if (selectedWidget == null) {
            selectedWidget = tabAcc.mcaccess$getRoot();
            selectedWidgetRef = new WeakReference<>(selectedWidget);
            if (selectedWidget != null) {
                announceWidget(selectedWidget, true);
            }
            return;
        }

        AccessorAdvancementWidget widgetAcc = (AccessorAdvancementWidget) selectedWidget;
        AdvancementWidget target = null;

        if (dirX > 0) { // Right -> Child
            List<AdvancementWidget> children = widgetAcc.mcaccess$getChildren();
            if (children != null && !children.isEmpty()) {
                target = children.get(0);
            }
        } else if (dirX < 0) { // Left -> Parent
            target = widgetAcc.mcaccess$getParent();
        } else if (dirY != 0) { // Up / Down -> Siblings
            AdvancementWidget parent = widgetAcc.mcaccess$getParent();
            if (parent != null) {
                AccessorAdvancementWidget parentAcc = (AccessorAdvancementWidget) parent;
                List<AdvancementWidget> siblings = parentAcc.mcaccess$getChildren();
                if (siblings != null && siblings.size() > 1) {
                    int idx = siblings.indexOf(selectedWidget);
                    if (idx >= 0) {
                        int nextIdx = idx + (dirY > 0 ? 1 : -1);
                        if (nextIdx >= 0 && nextIdx < siblings.size()) {
                            target = siblings.get(nextIdx);
                        }
                    }
                }
            }
        }

        // Spatial fallback if tree structure didn't find adjacent
        if (target == null && tabAcc.mcaccess$getWidgets() != null) {
            target = findClosestInDirection(selectedWidget, tabAcc.mcaccess$getWidgets().values(), dirX, dirY);
        }

        if (target != null) {
            selectedWidgetRef = new WeakReference<>(target);
            announceWidget(target, true);
        }
    }

    private static AdvancementWidget findClosestInDirection(AdvancementWidget current, Collection<AdvancementWidget> all, int dirX, int dirY) {
        int curX = current.getX();
        int curY = current.getY();
        AdvancementWidget best = null;
        double bestDist = Double.MAX_VALUE;

        for (AdvancementWidget other : all) {
            if (other == current) continue;
            int dx = other.getX() - curX;
            int dy = other.getY() - curY;

            if (dirX > 0 && dx <= 0) continue;
            if (dirX < 0 && dx >= 0) continue;
            if (dirY > 0 && dy <= 0) continue;
            if (dirY < 0 && dy >= 0) continue;

            double dist = (dx * dx) + (dy * dy);
            if (dist < bestDist) {
                bestDist = dist;
                best = other;
            }
        }
        return best;
    }

    public static void repeatCurrent() {
        AdvancementWidget current = selectedWidgetRef.get();
        if (current != null) {
            announceWidget(current, true);
        }
    }

    public static void announceWidget(AdvancementWidget widget, boolean interrupt) {
        if (widget == null) return;
        DisplayInfo display = widget.getDisplay();
        if (display == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append(display.title().getString());

        String typeStr = switch (display.type()) {
            case TASK -> "Обычное";
            case GOAL -> "Цель";
            case CHALLENGE -> "Испытание";
        };
        sb.append(". ").append(typeStr);

        AccessorAdvancementWidget widgetAcc = (AccessorAdvancementWidget) widget;
        AdvancementProgress progress = widgetAcc.mcaccess$getProgress();
        if (progress != null) {
            if (progress.isDone()) {
                sb.append(". Выполнено.");
            } else {
                sb.append(". Не выполнено");
                if (progress.hasProgress() && progress.getPercent() > 0) {
                    sb.append(", прогресс ").append(Math.round(progress.getPercent() * 100)).append("%");
                }
                sb.append(".");
            }
        }

        sb.append(" ").append(display.description().getString());

        SpeechEngine.getInstance().speak(sb.toString(), interrupt);
    }
}
