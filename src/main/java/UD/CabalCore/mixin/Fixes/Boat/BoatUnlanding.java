package UD.CabalCore.mixin.Fixes.Boat;

import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatUnlanding {

    @Shadow
    private Boat.Status getStatus() {
        throw new AssertionError();
    }

    @Inject(method = "controlBoat", at = @At("HEAD"), cancellable = true)
    private void cc$disableBoatInputOnLand(CallbackInfo ci) {
        if (this.getStatus() == Boat.Status.ON_LAND) {
            ci.cancel();
        }
    }
}