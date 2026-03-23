package UD.CabalCore.mixin.RBDeconstructor;

import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class RBServerOffliner {

    @Inject(method = "handlePlaceRecipe", at = @At("HEAD"), cancellable = true)
    private void cc$killHandlePlaceRecipe(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "handleRecipeBookChangeSettingsPacket", at = @At("HEAD"), cancellable = true)
    private void cc$killHandleBookSettings(ServerboundRecipeBookChangeSettingsPacket packet, CallbackInfo ci) {
        ci.cancel();
    }
}