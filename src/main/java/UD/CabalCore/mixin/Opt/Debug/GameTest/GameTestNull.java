package UD.CabalCore.mixin.Opt.Debug.GameTest;

import UD.CabalCore.mixin.Opt.Flags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.GameTestDebugRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(GameTestDebugRenderer.class)
public abstract class GameTestNull {

    @Shadow @Final private Map<BlockPos, ?> markers;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cc$killEmptyRender(PoseStack poseStack,
                                    MultiBufferSource bufferSource,
                                    double camX,
                                    double camY,
                                    double camZ,
                                    CallbackInfo ci) {
        if (!Flags.DEBUG_GAMETEST_NULLER) {
            return;
        }

        if (this.markers.isEmpty()) {
            ci.cancel();
        }
    }
}