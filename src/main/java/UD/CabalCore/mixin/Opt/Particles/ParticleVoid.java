package UD.CabalCore.mixin.Opt.Particles;

import UD.CabalCore.mixin.Opt.Flags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public abstract class ParticleVoid {

    @Shadow @Final private Map<ParticleRenderType, Queue<Particle>> particles;
    @Shadow @Final private Queue<Particle> particlesToAdd;
    @Shadow @Final private Queue<?> trackingEmitters;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cc$voidRender(PoseStack poseStack,
                               MultiBufferSource.BufferSource bufferSource,
                               LightTexture lightTexture,
                               Camera camera,
                               float partialTick,
                               CallbackInfo ci) {
        if (!Flags.PARTICLE_VOID) {
            return;
        }

        if (!this.particlesToAdd.isEmpty()) {
            return;
        }

        if (!this.trackingEmitters.isEmpty()) {
            return;
        }

        if (!this.particles.isEmpty()) {
            for (Queue<Particle> queue : this.particles.values()) {
                if (queue != null && !queue.isEmpty()) {
                    return;
                }
            }
        }

        ci.cancel();
    }
}