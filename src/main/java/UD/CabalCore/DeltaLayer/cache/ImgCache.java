package UD.CabalCore.DeltaLayer.cache;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImgCache {
    private static final Map<ResourceLocation, NativeImage> CACHE = new ConcurrentHashMap<>();

    private ImgCache() {
    }

    public static NativeImage get(ResourceLocation skin) {
        NativeImage cached = CACHE.get(skin);
        if (cached != null) {
            return cached;
        }

        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(skin);
        if (texture instanceof TextureAccessor accessor) {
            NativeImage image = accessor.cc$getDeltaLayerImage();
            if (image != null) {
                CACHE.put(skin, image);
                return image;
            }
        }

        return null;
    }

    public static void invalidate(ResourceLocation skin) {
        CACHE.remove(skin);
    }

    public static void clear() {
        CACHE.clear();
    }
}