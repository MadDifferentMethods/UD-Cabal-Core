package UD.CabalCore.mixin.LangOp;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ClientLanguage.class)
public abstract class EnLock {

    @Shadow @Final @Mutable
    private boolean defaultRightToLeft;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cc$forceCtorBidiOff(CallbackInfo ci) {
        this.defaultRightToLeft = false;
    }

    @Inject(method = "isDefaultRightToLeft", at = @At("HEAD"), cancellable = true)
    private void cc$forceBidiOff(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "getVisualOrder", at = @At("HEAD"), cancellable = true)
    private void cc$forcePlainVisualOrder(FormattedText text, CallbackInfoReturnable<FormattedCharSequence> cir) {
        cir.setReturnValue(sink ->
                text.visit((style, str) ->
                                StringDecomposer.iterateFormatted(str, style, sink)
                                        ? Optional.empty()
                                        : FormattedText.STOP_ITERATION,
                        Style.EMPTY
                ).isPresent()
        );
    }
}