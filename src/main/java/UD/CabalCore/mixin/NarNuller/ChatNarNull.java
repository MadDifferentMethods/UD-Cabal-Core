package UD.CabalCore.mixin.NarNuller;

import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatNarNull {

    @Inject(method = "narrateChatMessage", at = @At("HEAD"), cancellable = true)
    private void cc$killNarrateChatMessage(ChatType.Bound bound, Component component, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
    private void cc$killHandleSystemMessage(Component component, boolean overlay, CallbackInfo ci) {
        ci.cancel();
    }
}