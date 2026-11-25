package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.navigation.EditBoxNarrationHelper;
import com.github.lostcra.mcaccessibility.navigation.ScreenTextHelper;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationTrigger;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Shadow public abstract Component getTitle();
    @Shadow private long narrationSuppressTime;

    @Shadow private void scheduleNarration(long delay, boolean suppressPrevious, NarrationTrigger trigger) {}
    @Shadow protected void changeFocus(ComponentPath path) {}

    @Inject(method = "shouldRunNarration", at = @At("HEAD"), cancellable = true)
    private void onShouldRunNarration(CallbackInfoReturnable<Boolean> cir) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "afterKeyboardAction", at = @At("HEAD"), cancellable = true)
    private void onAfterKeyboardAction(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            this.scheduleNarration(100L, true, NarrationTrigger.KEYBOARD);
            ci.cancel();
        }
    }

    @Inject(method = "afterMouseMove", at = @At("HEAD"), cancellable = true)
    private void onAfterMouseMove(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            this.scheduleNarration(250L, true, NarrationTrigger.MOUSE);
            ci.cancel();
        }
    }

    @Inject(method = "afterMouseAction", at = @At("HEAD"), cancellable = true)
    private void onAfterMouseAction(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            this.scheduleNarration(100L, true, NarrationTrigger.MOUSE);
            ci.cancel();
        }
    }

    @Inject(method = "scheduleNarration(JZLnet/minecraft/client/gui/narration/NarrationTrigger;)V", at = @At("HEAD"))
    private void onScheduleNarration(long delay, boolean suppressPrevious, NarrationTrigger trigger, CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (config.isMasterEnabled() && config.isScreenNarrationEnabled()) {
            this.narrationSuppressTime = Long.MIN_VALUE;
        }
    }

    @Inject(method = "getNarrationMessage", at = @At("RETURN"), cancellable = true)
    private void onGetNarrationMessage(CallbackInfoReturnable<Component> cir) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        Component original = cir.getReturnValue();
        String str = original != null ? original.getString().trim() : "";
        String body = ScreenTextHelper.collectBodyNarration((Screen) (Object) this, str);
        if (!body.isEmpty()) {
            str = str.isEmpty() ? body : str + ". " + body;
        }
        if (!str.isEmpty() && !str.startsWith("Экран") && !str.startsWith("Screen")) {
            cir.setReturnValue(Component.literal("Экран: " + str));
        } else if (!body.isEmpty()) {
            cir.setReturnValue(Component.literal(str));
        }
    }

    @Inject(method = "addRenderableOnly", at = @At("RETURN"))
    private <T extends Renderable> void onAddRenderableOnly(T renderable, CallbackInfoReturnable<T> cir) {
        ScreenTextHelper.ensureRenderableTextRegistered((Screen) (Object) this, renderable);
    }

    @Inject(method = "setInitialFocus()V", at = @At("RETURN"))
    private void onSetInitialFocus(CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        ContainerEventHandler handler = (ContainerEventHandler) (Object) this;
        GuiEventListener initial = handler.getFocused();
        if (!ScreenTextHelper.isPassiveTextWidget(initial)) {
            return;
        }
        Set<GuiEventListener> visited = new HashSet<>();
        visited.add(initial);
        int maxSteps = handler.children().size();
        for (int i = 0; i < maxSteps; i++) {
            ComponentPath next = handler.nextFocusPath(new FocusNavigationEvent.TabNavigation(true));
            if (next == null) {
                break;
            }
            this.changeFocus(next);
            GuiEventListener current = handler.getFocused();
            if (current == null || !visited.add(current)) {
                break;
            }
            if (!ScreenTextHelper.isPassiveTextWidget(current)) {
                return;
            }
        }
        // No interactive widget found; keep initial passive text widget focused
        ComponentPath fallback = initial.nextFocusPath(new FocusNavigationEvent.InitialFocus());
        if (fallback != null) {
            this.changeFocus(ComponentPath.path(handler, fallback));
        }
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"))
    private void onExtractRenderStateStart(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ScreenTextHelper.onScreenRenderStart((Screen) (Object) this);
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("RETURN"))
    private void onExtractRenderStateEnd(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ScreenTextHelper.onScreenRenderEnd((Screen) (Object) this);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        ScreenTextHelper.onScreenRemoved((Screen) (Object) this);
        EditBoxNarrationHelper.onScreenRemoved();
    }

    @Inject(method = "triggerImmediateNarration", at = @At("HEAD"), cancellable = true)
    private void onTriggerImmediateNarration(boolean interrupt, CallbackInfo ci) {
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled() || !config.isScreenNarrationEnabled()) {
            return;
        }
        if ((Object) this instanceof AbstractContainerScreen && config.isInventoryNarrationEnabled()) {
            ci.cancel();
            return;
        }
        if ((Object) this instanceof AdvancementsScreen && config.isAdvancementNarrationEnabled()) {
            ci.cancel();
            return;
        }
    }
}
