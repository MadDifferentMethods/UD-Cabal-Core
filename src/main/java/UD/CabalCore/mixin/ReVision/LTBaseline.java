package UD.CabalCore.mixin.ReVision;

import com.mojang.math.Vector3f;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public abstract class LTBaseline {

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LightTexture;getBrightness(Lnet/minecraft/world/level/dimension/DimensionType;I)F"
            )
    )
    private float cc$noAmbientFloor(DimensionType dimensionType, int lightLevel) {
        float f = (float) lightLevel / 15.0F;
        return f / (4.0F - 3.0F * f);
    }

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getWaterVision()F"
            )
    )
    private float cc$noWaterVisionInLightmap(LocalPlayer player) {
        return 0.0F;
    }

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F"
            )
    )
    private float cc$noNightVisionInLightmap(LivingEntity entity, float partialTick) {
        return 0.0F;
    }

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
                    ordinal = 1
            )
    )
    private Object cc$zeroGamma(OptionInstance<Double> option) {
        return 0.0D;
    }

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/math/Vector3f;lerp(Lcom/mojang/math/Vector3f;F)V"
            )
    )
    private void cc$stripMinimumLightLerp(Vector3f self, Vector3f target, float amount) {
        if (amount == 0.04F
                && Mth.equal(target.x(), 0.75F)
                && Mth.equal(target.y(), 0.75F)
                && Mth.equal(target.z(), 0.75F)) {
            return;
        }

        self.lerp(target, amount);
    }
}