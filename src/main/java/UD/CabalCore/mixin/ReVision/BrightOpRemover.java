package UD.CabalCore.mixin.ReVision;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

    @Mixin(VideoSettingsScreen.class)
    public abstract class BrightOpRemover {

        @Inject(method = "options", at = @At("RETURN"), cancellable = true)
        private static void cc$removeBrightnessOption(Options options, CallbackInfoReturnable<OptionInstance<?>[]> cir) {
            OptionInstance<?>[] original = cir.getReturnValue();
            OptionInstance<?> gamma = options.gamma();

            List<OptionInstance<?>> filtered = new ArrayList<>(original.length);
            for (OptionInstance<?> option : original) {
                if (option != gamma) {
                    filtered.add(option);
                }
            }

            cir.setReturnValue(filtered.toArray(new OptionInstance<?>[0]));
        }
    }