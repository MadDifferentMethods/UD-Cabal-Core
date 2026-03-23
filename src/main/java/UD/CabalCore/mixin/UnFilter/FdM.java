package UD.CabalCore.mixin.UnFilter;

import net.minecraft.network.chat.FilterMask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FilterMask.class)
public class FdM {

    // 1. Force 'apply' to always return the raw text.
    // This kills the actual filtering logic. Regardless of what the mask contains,
    // it will render the original string.
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void stripFilterMask(String rawText, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(rawText);
    }

    // 2. Force 'isEmpty' to return true.
    // This tells the game "this mask does nothing", which skips processing logic
    // in other parts of the code (like SignBlockEntity or FilteredText).
    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
    private void alwaysEmpty(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}