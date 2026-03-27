package UD.CabalCore.mixin.Opt.Sky;

import UD.CabalCore.Opt.Sky.SkyColorCache;
import UD.CabalCore.mixin.Opt.Flags;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FogRenderer.class)
public abstract class FogSkyColorCache {

    @Redirect(
            method = "setupColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getSkyColor(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private static Vec3 cc$cachedSkyColorForFog(ClientLevel level, Vec3 pos, float partialTick) {
        if (!Flags.SKY_COLOR_CACHE) {
            return level.getSkyColor(pos, partialTick);
        }

        return SkyColorCache.getForFog(level, pos, partialTick);
    }
}