package UD.CabalCore.mixin.ReVision;

import UD.CabalCore.ReVision.EyeAdapt;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public abstract class AdaExLiTex {

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"
            )
    )
    private void cc$applyAdaptiveExposure(NativeImage image, int x, int y, int rgba) {
        image.setPixelRGBA(x, y, EyeAdapt.applyToPackedColor(rgba));
    }
}