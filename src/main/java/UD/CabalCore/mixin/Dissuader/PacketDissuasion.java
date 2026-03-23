package UD.CabalCore.mixin.Dissuader;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class PacketDissuasion {

    @Inject(
            method = "handleCommandSuggestions",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cc$killCommandSuggestions(ClientboundCommandSuggestionsPacket packet, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "handleCustomChatCompletions",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cc$killCustomCompletions(ClientboundCustomChatCompletionsPacket packet, CallbackInfo ci) {
        ci.cancel();
    }
}