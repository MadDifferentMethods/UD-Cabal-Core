package UD.CabalCore.mixin.NarNuller;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class NarCP {

    @Redirect(
            method = "handleSetEntityPassengersPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void cc$skipMountOverlayMessage(Gui gui, Component component, boolean animateColor) {
        // swallow mount overlay message
    }

    @Redirect(
            method = "handleSetEntityPassengersPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getNarrator()Lnet/minecraft/client/GameNarrator;"
            )
    )
    private GameNarrator cc$skipMountGetNarrator(Minecraft minecraft) {
        // null it at the caller site, consistent with NarNuller approach
        return null;
    }

    @Redirect(
            method = "handleSetEntityPassengersPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/GameNarrator;sayNow(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void cc$skipMountNarration(GameNarrator narrator, Component component) {
        // swallow follow-up call so null receiver never matters
    }
}