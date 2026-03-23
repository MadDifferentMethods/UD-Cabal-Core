package UD.CabalCore.mixin.NarNuller;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardHandler.class)
public class NarKeyBypass {


    @Redirect(
            method = "keyPress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getNarrator()Lnet/minecraft/client/GameNarrator;"
            )
    )
    private GameNarrator cc$skipNarratorGetter(Minecraft instance) {
        return null;
    }

    @Redirect(
            method = "keyPress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/GameNarrator;isActive()Z"
            )
    )
    private boolean cc$forceInactive(GameNarrator narrator) {
        return false;
    }

}