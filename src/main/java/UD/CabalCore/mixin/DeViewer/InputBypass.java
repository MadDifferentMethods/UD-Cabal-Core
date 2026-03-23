package UD.CabalCore.mixin.DeViewer;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class InputBypass {

    @Shadow
    public abstract String normalizeChatMessage(String text);

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void cc$killPreviewHandleChatInput(String text, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        text = this.normalizeChatMessage(text);

        if (text.isEmpty()) {
            cir.setReturnValue(true);
            return;
        }

        if (addToHistory) {
            net.minecraft.client.Minecraft.getInstance().gui.getChat().addRecentChat(text);
        }

        Component previewResponse = null;

        if (text.startsWith("/")) {
            net.minecraft.client.Minecraft.getInstance().player.commandSigned(text.substring(1), previewResponse);
        } else {
            net.minecraft.client.Minecraft.getInstance().player.chatSigned(text, previewResponse);
        }

        cir.setReturnValue(true);
    }
}