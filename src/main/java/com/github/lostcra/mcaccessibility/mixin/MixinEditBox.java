package com.github.lostcra.mcaccessibility.mixin;

import com.github.lostcra.mcaccessibility.navigation.EditBoxNarrationHelper;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EditBox.class)
public abstract class MixinEditBox {

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void onKeyPressedHead(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        EditBoxNarrationHelper.beforeKeyPressed((EditBox) (Object) this);
    }

    @Inject(method = "keyPressed", at = @At("RETURN"))
    private void onKeyPressedReturn(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            EditBoxNarrationHelper.afterKeyPressed((EditBox) (Object) this, event);
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"))
    private void onCharTypedHead(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
        EditBoxNarrationHelper.beforeCharTyped((EditBox) (Object) this);
    }

    @Inject(method = "charTyped", at = @At("RETURN"))
    private void onCharTypedReturn(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            EditBoxNarrationHelper.afterCharTyped((EditBox) (Object) this, event);
        }
    }

    @Inject(method = "onClick", at = @At("RETURN"))
    private void onOnClickReturn(MouseButtonEvent event, boolean doubleClick, CallbackInfo ci) {
        EditBoxNarrationHelper.afterClick((EditBox) (Object) this);
    }
}
