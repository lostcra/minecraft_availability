package com.github.lostcra.mcaccessibility;

import com.github.lostcra.mcaccessibility.commands.AccessibilityCommands;
import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecraftAvailabilityMod implements ClientModInitializer {
    public static final String MOD_ID = "mcaccessibility";
    public static final Logger LOGGER = LoggerFactory.getLogger("MinecraftAvailability");

    public static KeyMapping KEY_TOGGLE_VOICEOVER;
    public static KeyMapping KEY_TOGGLE_CHAT;
    public static KeyMapping KEY_OPEN_SETTINGS;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Minecraft Availability Mod...");

        // Load configuration
        AccessibilityConfig.load();

        // Initialize Speech Engine
        SpeechEngine.getInstance();

        // Register Key Mappings
        registerKeyMappings();

        // Register client commands
        AccessibilityCommands.register();

        LOGGER.info("Minecraft Availability Mod initialized successfully.");
    }

    private void registerKeyMappings() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "general")
        );

        KEY_TOGGLE_VOICEOVER = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mcaccessibility.toggle_voiceover",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_A,
                category
        ));

        KEY_TOGGLE_CHAT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mcaccessibility.toggle_chat",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_C,
                category
        ));

        KEY_OPEN_SETTINGS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mcaccessibility.settings",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_S,
                category
        ));
    }
}
