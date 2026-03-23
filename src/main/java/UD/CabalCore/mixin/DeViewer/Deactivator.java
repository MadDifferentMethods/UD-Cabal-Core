package UD.CabalCore.mixin.DeViewer;

import net.minecraft.client.gui.chat.ChatPreviewAnimator;
import net.minecraft.client.gui.chat.ClientChatPreview;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.ChatPreviewStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class Deactivator {

    @Shadow private ClientChatPreview chatPreview;
    @Shadow private ChatPreviewStatus chatPreviewStatus;
    @Shadow private boolean previewNotRequired;

    @Shadow @Final @Mutable
    private ChatPreviewAnimator chatPreviewAnimator;

    @Inject(method = "init", at = @At("TAIL"))
    private void cc$hardKillPreviewInitState(CallbackInfo ci) {
        this.chatPreview = null;
        this.chatPreviewStatus = ChatPreviewStatus.OFF;
        this.previewNotRequired = true;
        this.chatPreviewAnimator = new ChatPreviewAnimator();
    }
}