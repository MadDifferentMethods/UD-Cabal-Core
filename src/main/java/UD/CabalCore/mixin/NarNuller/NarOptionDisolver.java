package UD.CabalCore.mixin.NarNuller;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(AccessibilityOptionsScreen.class)
public class NarOptionDisolver {

    @Inject(method = "options", at = @At("RETURN"), cancellable = true)
    private static void cc$removeNarratorOption(Options options, CallbackInfoReturnable<OptionInstance<?>[]> cir) {
        OptionInstance<?>[] original = cir.getReturnValue();
        OptionInstance<?> narrator = options.narrator();

        List<OptionInstance<?>> filtered = new ArrayList<>(original.length);
        for (OptionInstance<?> opt : original) {
            if (opt != narrator) {
                filtered.add(opt);
            }
        }
        cir.setReturnValue(filtered.toArray(new OptionInstance<?>[0]));
    }
}