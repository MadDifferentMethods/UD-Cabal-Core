package UD.CabalCore.mixin.UnFilter;

import net.minecraft.server.network.FilteredText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FilteredText.class)
public class FFT {

    // Ensure the record always reports as "unfiltered" to bypass logic checks.
    @Inject(method = "isFiltered", at = @At("HEAD"), cancellable = true)
    private void neverFiltered(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    // Ensure the 'filtered' accessor returns raw text.
    // Note: We cannot inject into 'raw()' or 'mask()' easily as they are record components,
    // but 'filtered()' computes the result.
    @Inject(method = "filtered", at = @At("HEAD"), cancellable = true)
    private void alwaysRawString(CallbackInfoReturnable<String> cir) {
        // We cast to access the raw field of the record.
        // This effectively ignores the mask.
        cir.setReturnValue(((FilteredText)(Object)this).raw());
    }
}