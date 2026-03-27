package UD.CabalCore.mixin.Opt.Fov;

import UD.CabalCore.mixin.Opt.Flags;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class FovZeroBypass {

    @Shadow @Final private net.minecraft.client.Minecraft minecraft;

    @Inject(
            method = "getFov(Lnet/minecraft/client/Camera;FZ)D",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void cc$zeroBypass(Camera camera,
                               float partialTick,
                               boolean useFovSetting,
                               CallbackInfoReturnable<Double> cir) {
        if (!Flags.FOV_ZERO_BYPASS) {
            return;
        }

        if (!useFovSetting) {
            return;
        }

        Options options = this.minecraft.options;
        double scale = ((Double) options.fovEffectScale().get()).doubleValue();

        if (scale <= 0.0D) {
            cir.setReturnValue((double) (Integer) options.fov().get());
        }
    }
}