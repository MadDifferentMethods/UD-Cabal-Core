package UD.CabalCore.mixin.DeltaLayer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.HttpTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import UD.CabalCore.DeltaLayer.cache.TextureAccessor;

@Mixin(HttpTexture.class)
public abstract class DeltaLayerExposer implements TextureAccessor {

    @Unique
    private NativeImage cc$deltaLayerImage;

    @Inject(method = "upload", at = @At("HEAD"))
    private void cc$captureUploadedSkin(NativeImage image, CallbackInfo ci) {
        if (image == null) {
            this.cc$deltaLayerImage = null;
            return;
        }

        if (this.cc$deltaLayerImage != null) {
            this.cc$deltaLayerImage.close();
            this.cc$deltaLayerImage = null;
        }

        // Correct constructor for 1.19.2
        NativeImage copy = new NativeImage(image.getWidth(), image.getHeight(), true);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                copy.setPixelRGBA(x, y, image.getPixelRGBA(x, y));
            }
        }

        this.cc$deltaLayerImage = copy;
        System.out.println("[DeltaLayer] HttpTexture upload copied: " + copy);
    }

    @Override
    public NativeImage cc$getDeltaLayerImage() {
        return this.cc$deltaLayerImage;
    }
}