package com.github.lostcra.mcaccessibility.config;

import net.minecraft.client.gui.screens.Screen;

public class AccessibilityConfigScreen {

    public static Screen create(Screen parent) {
        return new AccessibilitySettingsScreen(parent);
    }
}
