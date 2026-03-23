package UD.CabalCore.mixin.NarNuller;

import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Screen.class)
public class GlNarNull {

    @Inject(method = "afterMouseMove", at = @At("HEAD"), cancellable = true)
    private void cc$killAfterMouseMove(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "afterMouseAction", at = @At("HEAD"), cancellable = true)
    private void cc$killAfterMouseAction(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "afterKeyboardAction", at = @At("HEAD"), cancellable = true)
    private void cc$killAfterKeyboardAction(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "handleDelayedNarration", at = @At("HEAD"), cancellable = true)
    private void cc$killHandleDelayedNarration(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "triggerImmediateNarration", at = @At("HEAD"), cancellable = true)
    private void cc$killTriggerImmediateNarration(boolean immediate, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "runNarration", at = @At("HEAD"), cancellable = true)
    private void cc$killRunNarration(boolean immediate, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "shouldRunNarration", at = @At("HEAD"), cancellable = true)
    private void cc$killShouldRunNarration(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "updateNarrationState", at = @At("HEAD"), cancellable = true)
    private void cc$killUpdateNarrationState(NarrationElementOutput output, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "updateNarratedWidget", at = @At("HEAD"), cancellable = true)
    private void cc$killUpdateNarratedWidget(NarrationElementOutput output, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "findNarratableWidget", at = @At("HEAD"), cancellable = true)
    private static void cc$killFindNarratableWidget(List<? extends NarratableEntry> list,
                                                    NarratableEntry last,
                                                    CallbackInfoReturnable<Screen.NarratableSearchResult> cir) {
        cir.setReturnValue(null);
    }

    @Inject(method = "narrationEnabled", at = @At("HEAD"), cancellable = true)
    private void cc$killNarrationEnabled(CallbackInfo ci) {
        ci.cancel();
    }
}