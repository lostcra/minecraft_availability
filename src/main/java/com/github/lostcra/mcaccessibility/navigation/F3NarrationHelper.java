package com.github.lostcra.mcaccessibility.navigation;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;
import java.util.Optional;

public class F3NarrationHelper {

    public static void onOverlayVisibilityChanged(boolean visible) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isF3NarrationEnabled()) {
            return;
        }

        if (!visible) {
            SpeechEngine.getInstance().speak("Меню F3 закрыто.", true);
        } else {
            narrateF3Summary(Minecraft.getInstance());
        }
    }

    public static void narrateF3Summary(Minecraft mc) {
        if (mc == null) return;
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled()) return;

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        BlockPos pos = player.blockPosition();
        Direction dir = player.getDirection();
        int light = mc.level.getMaxLocalRawBrightness(pos);
        String biome = getBiomeName(mc.level, pos);
        String target = getTargetInfo(mc);
        long day = mc.level.getLevelData().getGameTime() / 24000L;
        String dim = getDimensionName(mc.level);

        StringBuilder sb = new StringBuilder();
        sb.append("Меню F3. Координаты: X ").append(String.format(Locale.ROOT, "%.1f", x));
        sb.append(", Y ").append(String.format(Locale.ROOT, "%.1f", y));
        sb.append(", Z ").append(String.format(Locale.ROOT, "%.1f", z));
        sb.append(". Блок: ").append(pos.getX()).append(" ").append(pos.getY()).append(" ").append(pos.getZ());
        sb.append(". Направление: ").append(getDirectionNameRu(dir));
        sb.append(". Биом: ").append(biome);
        sb.append(". Свет: ").append(light);
        if (!target.isEmpty()) {
            sb.append(". ").append(target);
        }
        sb.append(". День ").append(day);
        if (!dim.isEmpty()) {
            sb.append(" (").append(dim).append(")");
        }
        sb.append(".");

        SpeechEngine.getInstance().speak(sb.toString(), true);
    }

    public static String getDirectionNameRu(Direction dir) {
        if (dir == null) return "Неизвестно";
        switch (dir) {
            case NORTH: return "Север (Z-)";
            case SOUTH: return "Юг (Z+)";
            case WEST: return "Запад (X-)";
            case EAST: return "Восток (X+)";
            case UP: return "Вверх (Y+)";
            case DOWN: return "Вниз (Y-)";
            default: return dir.getName();
        }
    }

    public static String getBiomeName(Level level, BlockPos pos) {
        try {
            Holder<Biome> holder = level.getBiome(pos);
            Optional<ResourceKey<Biome>> key = holder.unwrapKey();
            if (key.isPresent()) {
                Identifier id = key.get().identifier();
                String transKey = "biome." + id.getNamespace() + "." + id.getPath();
                Component comp = Component.translatable(transKey);
                String str = comp.getString();
                if (!str.equals(transKey)) {
                    return str;
                }
                return id.getPath();
            }
        } catch (Throwable ignored) {}
        return "Неизвестный биом";
    }

    public static String getTargetInfo(Minecraft mc) {
        try {
            HitResult hit = mc.hitResult;
            if (hit != null && mc.level != null) {
                if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
                    BlockPos bpos = bhr.getBlockPos();
                    BlockState state = mc.level.getBlockState(bpos);
                    if (!state.isAir()) {
                        return "Смотрит на блок: " + state.getBlock().getName().getString() + " (" + bpos.getX() + ", " + bpos.getY() + ", " + bpos.getZ() + ")";
                    }
                } else if (hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult ehr) {
                    Entity ent = ehr.getEntity();
                    return "Смотрит на существо: " + ent.getDisplayName().getString();
                }
            }
        } catch (Throwable ignored) {}
        return "";
    }

    public static String getDimensionName(Level level) {
        try {
            String path = level.dimension().identifier().getPath();
            if ("overworld".equalsIgnoreCase(path)) return "Верхний мир";
            if ("the_nether".equalsIgnoreCase(path)) return "Нижний мир";
            if ("the_end".equalsIgnoreCase(path)) return "Энд";
            return path;
        } catch (Throwable ignored) {}
        return "";
    }
}
