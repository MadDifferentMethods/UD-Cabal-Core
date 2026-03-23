package UD.CabalCore.mixin;

import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class DeTelemetry {

    @Inject(method = "createTelemetryManager", at = @At("HEAD"), cancellable = true)
    private void cc$killTelemetryFactory(CallbackInfoReturnable<ClientTelemetryManager> cir) {
        cir.setReturnValue(null);
    }
}