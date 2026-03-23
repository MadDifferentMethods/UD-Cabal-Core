package UD.CabalCore.mixin.RBDeconstructor;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookComponent.class)
public class RBLineCut {

    @Inject(method = "sendUpdateSettings", at = @At("HEAD"), cancellable = true)
    private void cc$killSendUpdateSettings(CallbackInfo ci) {
        ci.cancel();
    }
}