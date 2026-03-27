package UD.CabalCore.mixin.Opt.Debug.GameTest;

import UD.CabalCore.mixin.Opt.Flags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.GameTestDebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DebugRenderer.class)
public abstract class GameTestStrip {

    @Shadow @Final public GameTestDebugRenderer gameTestDebugRenderer;

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/debug/GameTestDebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;DDD)V"
            )
    )
    private void cc$stripEmptyGameTest(GameTestDebugRenderer instance,
                                       PoseStack poseStack,
                                       MultiBufferSource bufferSource,
                                       double camX,
                                       double camY,
                                       double camZ) {
        if (!Flags.DEBUG_GAMETEST_NULLER) {
            instance.render(poseStack, bufferSource, camX, camY, camZ);
            return;
        }

        if (((GameTestMarkerAccessor) instance).cc$getMarkers().isEmpty()) {
            return;
        }

        instance.render(poseStack, bufferSource, camX, camY, camZ);
    }
}