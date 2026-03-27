package UD.CabalCore.mixin.Opt.Toast;

import UD.CabalCore.mixin.Opt.Flags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.Deque;
import java.util.List;

@Mixin(ToastComponent.class)
public abstract class ToastObliterator {

    @Shadow @Final private List<?> visible;
    @Shadow @Final private Deque<Toast> queued;
    @Shadow @Final private BitSet occupiedSlots;

    @Unique
    private void cc$toastPurge() {
        this.visible.clear();
        this.queued.clear();
        this.occupiedSlots.clear();
    }

    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void cc$obliterateAddedToast(Toast toast, CallbackInfo ci) {
        if (!Flags.TOAST_OBLITERATOR) {
            return;
        }

        ci.cancel();
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void cc$hardClear(CallbackInfo ci) {
        if (!Flags.TOAST_OBLITERATOR) {
            return;
        }

        this.cc$toastPurge();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cc$obliterateToastRender(PoseStack poseStack, CallbackInfo ci) {
        if (!Flags.TOAST_OBLITERATOR) {
            return;
        }

        this.cc$toastPurge();
        ci.cancel();
    }
}