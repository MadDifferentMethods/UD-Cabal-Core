package UD.CabalCore.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class ManualJumper {

    @Shadow private boolean autoJumpEnabled;
    @Shadow private int autoJumpTime;

    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;updateAutoJump(FF)V"
            )
    )
    private void cc$skipUpdateAutoJump(LocalPlayer instance, float dx, float dz) {
        this.autoJumpEnabled = false;
        this.autoJumpTime = 0;
    }

    @Redirect(
            method = "sendPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"
            )
    )
    private Object cc$forceAutoJumpOptionFalse(OptionInstance<?> option) {
        return false;
    }

    @Inject(method = "isAutoJumpEnabled", at = @At("HEAD"), cancellable = true)
    private void cc$forceAutoJumpOff(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}