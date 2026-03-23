package UD.CabalCore.mixin.DeViewer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ReViewer {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cc$killTick(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onEdited", at = @At("HEAD"), cancellable = true)
    private void cc$killOnEdited(String text, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "updateChatPreview", at = @At("HEAD"), cancellable = true)
    private void cc$killUpdateChatPreview(String text, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "requestPreview", at = @At("HEAD"), cancellable = true)
    private void cc$killRequestPreview(String text, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "requestChatMessagePreview", at = @At("HEAD"), cancellable = true)
    private void cc$killRequestChatMessagePreview(String text, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "requestCommandArgumentPreview", at = @At("HEAD"), cancellable = true)
    private void cc$killRequestCommandArgumentPreview(String text, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "sendsChatPreviewRequests", at = @At("HEAD"), cancellable = true)
    private void cc$killSendsChatPreviewRequests(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "renderChatPreview", at = @At("HEAD"), cancellable = true)
    private void cc$killRenderChatPreview(PoseStack poseStack, Component component, float alpha, boolean signed, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "getDisplayedPreviewText", at = @At("HEAD"), cancellable = true)
    private void cc$killDisplayedPreviewText(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(null);
    }

    @Inject(method = "peekPreview", at = @At("HEAD"), cancellable = true)
    private void cc$killPeekPreview(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(null);
    }

    @Inject(method = "getChatPreviewStyleAt", at = @At("HEAD"), cancellable = true)
    private void cc$killPreviewStyleAt(double mouseX, double mouseY, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(null);
    }
}