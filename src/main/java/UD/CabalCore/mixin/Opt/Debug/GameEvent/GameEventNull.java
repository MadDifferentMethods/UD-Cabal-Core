package UD.CabalCore.mixin.Opt.Debug.GameEvent;

import UD.CabalCore.mixin.Opt.Flags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.GameEventListenerRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameEventListenerRenderer.class)
public abstract class GameEventNull {

    @Shadow @Final private List<?> trackedGameEvents;
    @Shadow @Final private List<?> trackedListeners;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cc$killEmptyRender(PoseStack poseStack,
                                    MultiBufferSource bufferSource,
                                    double camX,
                                    double camY,
                                    double camZ,
                                    CallbackInfo ci) {
        if (!Flags.DEBUG_GAMEEVENT_NULLER) {
            return;
        }

        if (this.trackedGameEvents.isEmpty() && this.trackedListeners.isEmpty()) {
            ci.cancel();
        }
    }
}