package UD.CabalCore.mixin.RBDeconstructor;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class RBOffliner {

    @Inject(method = "handleAddOrRemoveRecipes", at = @At("HEAD"), cancellable = true)
    private void cc$killRecipeBookAddRemove(ClientboundRecipePacket packet, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "handlePlaceRecipe", at = @At("HEAD"), cancellable = true)
    private void cc$killGhostRecipe(ClientboundPlaceGhostRecipePacket packet, CallbackInfo ci) {
        ci.cancel();
    }
}