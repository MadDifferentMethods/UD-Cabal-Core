package UD.CabalCore.mixin.NoMansRealm;

import com.mojang.realmsclient.client.RealmsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RealmsClient.class)
public class RCK {

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void cc$killRealmsClient(CallbackInfoReturnable<RealmsClient> cir) {
        cir.setReturnValue(null);
    }
}