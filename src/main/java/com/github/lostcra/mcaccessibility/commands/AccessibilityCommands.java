package com.github.lostcra.mcaccessibility.commands;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.config.AccessibilityConfigScreen;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public class AccessibilityCommands {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(AccessibilityCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        dispatcher.register(
            ClientCommands.literal("accessibility")
                .executes(ctx -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.execute(() -> mc.setScreenAndShow(AccessibilityConfigScreen.create(mc.gui.screen())));
                    return 1;
                })
                .then(ClientCommands.literal("tutorial")
                    .executes(ctx -> {
                        Minecraft mc = Minecraft.getInstance();
                        mc.execute(() -> mc.setScreenAndShow(new com.github.lostcra.mcaccessibility.config.TutorialScreen(mc.gui.screen())));
                        return 1;
                    }))
                .then(ClientCommands.literal("guide")
                    .executes(ctx -> {
                        Minecraft mc = Minecraft.getInstance();
                        mc.execute(() -> mc.setScreenAndShow(new com.github.lostcra.mcaccessibility.config.TutorialScreen(mc.gui.screen())));
                        return 1;
                    }))
                .then(ClientCommands.literal("toggle")
                    .executes(ctx -> {
                        AccessibilityConfig config = AccessibilityConfig.get();
                        boolean state = !config.isMasterEnabled();
                        config.setMasterEnabled(state);
                        config.save();
                        String msg = state ? "Озвучка включена" : "Озвучка отключена";
                        ctx.getSource().sendFeedback(Component.literal("[Доступность] " + msg));
                        SpeechEngine.getInstance().speak(msg, true);
                        return 1;
                    }))
                .then(ClientCommands.literal("chat")
                    .executes(ctx -> {
                        AccessibilityConfig config = AccessibilityConfig.get();
                        boolean state = !config.isChatNarrationEnabled();
                        config.setChatNarrationEnabled(state);
                        config.save();
                        String msg = state ? "Озвучивание чата включено" : "Озвучивание чата отключено";
                        ctx.getSource().sendFeedback(Component.literal("[Доступность] " + msg));
                        SpeechEngine.getInstance().speak(msg, true);
                        return 1;
                    }))
                .then(ClientCommands.literal("test")
                    .executes(ctx -> {
                        SpeechEngine.getInstance().testSpeech();
                        ctx.getSource().sendFeedback(Component.literal("[Доступность] Тестовое сообщение воспроизводится"));
                        return 1;
                    }))
                .then(ClientCommands.literal("rate")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(-10, 10))
                        .executes(ctx -> {
                            int val = IntegerArgumentType.getInteger(ctx, "value");
                            AccessibilityConfig config = AccessibilityConfig.get();
                            config.setSpeechRate(val);
                            config.save();
                            String msg = "Скорость речи установлена: " + val;
                            ctx.getSource().sendFeedback(Component.literal("[Доступность] " + msg));
                            SpeechEngine.getInstance().speak(msg, true);
                            return 1;
                        })))
                .then(ClientCommands.literal("volume")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> {
                            int val = IntegerArgumentType.getInteger(ctx, "value");
                            AccessibilityConfig config = AccessibilityConfig.get();
                            config.setSpeechVolume(val);
                            config.save();
                            String msg = "Громкость озвучки установлена: " + val + "%";
                            ctx.getSource().sendFeedback(Component.literal("[Доступность] " + msg));
                            SpeechEngine.getInstance().speak(msg, true);
                            return 1;
                        })))
        );

        // Alias /mcaccess
        dispatcher.register(
            ClientCommands.literal("mcaccess")
                .executes(ctx -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.execute(() -> mc.setScreenAndShow(AccessibilityConfigScreen.create(mc.gui.screen())));
                    return 1;
                })
                .then(ClientCommands.literal("tutorial")
                    .executes(ctx -> {
                        Minecraft mc = Minecraft.getInstance();
                        mc.execute(() -> mc.setScreenAndShow(new com.github.lostcra.mcaccessibility.config.TutorialScreen(mc.gui.screen())));
                        return 1;
                    }))
                .then(ClientCommands.literal("guide")
                    .executes(ctx -> {
                        Minecraft mc = Minecraft.getInstance();
                        mc.execute(() -> mc.setScreenAndShow(new com.github.lostcra.mcaccessibility.config.TutorialScreen(mc.gui.screen())));
                        return 1;
                    }))
        );
    }
}
