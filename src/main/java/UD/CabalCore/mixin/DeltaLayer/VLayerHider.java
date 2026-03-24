package UD.CabalCore.mixin.DeltaLayer;

import UD.CabalCore.DeltaLayer.render.Decider;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class VLayerHider {

    @Inject(method = "setModelProperties", at = @At("TAIL"))
    private void cc$hideVanillaOuterLayers(AbstractClientPlayer player, CallbackInfo ci) {
        if (!Decider.shouldRender(player)) {
            return;
        }

        PlayerModel<?> model = (PlayerModel<?>) ((VLayerAccessor) this).cc$getModel();

        model.hat.visible = false;
        model.jacket.visible = false;
        model.leftSleeve.visible = false;
        model.rightSleeve.visible = false;
        model.leftPants.visible = false;
        model.rightPants.visible = false;
    }
}