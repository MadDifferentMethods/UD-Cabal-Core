package UD.CabalCore.mixin;

import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public class PacketListenerBypass {

    @Redirect(
            method = "handleLogin",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/ClientTelemetryManager;onPlayerInfoReceived(Lnet/minecraft/world/level/GameType;Z)V"
            )
    )
    private void cc$skipPlayerInfo(ClientTelemetryManager manager, GameType type, boolean hardcore) {
        // no-op
    }

    @Redirect(
            method = "handleCustomPayload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/ClientTelemetryManager;onServerBrandReceived(Ljava/lang/String;)V"
            )
    )
    private void cc$skipServerBrand(ClientTelemetryManager manager, String brand) {
        // no-op
    }

    @Redirect(
            method = "onDisconnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/ClientTelemetryManager;onDisconnect()V"
            )
    )
    private void cc$skipDisconnect(ClientTelemetryManager manager) {
        // no-op
    }
}