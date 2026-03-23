package UD.CabalCore.mixin.DeViewer;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayChatPreviewPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class PacketCongester {

    @Inject(method = "handleChatPreview", at = @At("HEAD"), cancellable = true)
    private void cc$killHandleChatPreview(ClientboundChatPreviewPacket packet, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "handleSetDisplayChatPreview", at = @At("HEAD"), cancellable = true)
    private void cc$killHandleSetDisplayChatPreview(ClientboundSetDisplayChatPreviewPacket packet, CallbackInfo ci) {
        ci.cancel();
    }
}