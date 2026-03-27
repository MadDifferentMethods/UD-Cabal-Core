package UD.CabalCore.mixin.Opt.Sky;

import UD.CabalCore.Opt.Sky.SkyColorCache;
import UD.CabalCore.mixin.Opt.Flags;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class LevelSkyColorCache {

    @Redirect(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getSkyColor(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 cc$cachedSkyColorForSky(ClientLevel level, Vec3 pos, float partialTick) {
        if (!Flags.SKY_COLOR_CACHE) {
            return level.getSkyColor(pos, partialTick);
        }

        return SkyColorCache.getForSkyRender(level, pos, partialTick);
    }
}