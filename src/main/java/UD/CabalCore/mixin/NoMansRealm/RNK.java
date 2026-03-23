package UD.CabalCore.mixin.NoMansRealm;

import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsNotificationsScreen.class)
public class RNK {

    @Inject(method = "checkIfMcoEnabled", at = @At("HEAD"), cancellable = true)
    private void cc$killMcoAvailabilityCheck(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cc$killRealmsNotificationsTick(CallbackInfo ci) {
        ci.cancel();
    }
}