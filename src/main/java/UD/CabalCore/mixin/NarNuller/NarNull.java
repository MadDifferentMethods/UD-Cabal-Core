package UD.CabalCore.mixin.NarNuller;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class NarNull {

    @Inject(method = "getNarrator", at = @At("HEAD"), cancellable = true)
    private void cc$nullNarrator(CallbackInfoReturnable<GameNarrator> cir) {
        System.out.println("[ELR] Nulling Minecraft narrator");
        cir.setReturnValue(null);
    }
}