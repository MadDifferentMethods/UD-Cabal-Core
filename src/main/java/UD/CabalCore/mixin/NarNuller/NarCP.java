package UD.CabalCore.mixin.NarNuller;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public class NarCP {

    @Redirect(
            method = "handleSetEntityPassengersPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/GameNarrator;sayNow(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void cc$skipMountNarration(GameNarrator narrator, Component component) {
        // no-op
    }
}