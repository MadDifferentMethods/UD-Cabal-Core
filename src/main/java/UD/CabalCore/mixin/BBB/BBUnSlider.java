package UD.CabalCore.mixin.BBB;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionInstance.class)
public class BBUnSlider<T> {

    @Inject(
            method = "createButton(Lnet/minecraft/client/Options;III)Lnet/minecraft/client/gui/components/AbstractWidget;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cc$killBiomeBlendSlider(
            Options options,
            int x,
            int y,
            int width,
            CallbackInfoReturnable<AbstractWidget> cir
    ) {
        if ((Object) this == options.biomeBlendRadius()) {
            cir.setReturnValue(new Button(
                    -10000, -10000,
                    0, 0,
                    Component.empty(),
                    b -> {}
            ));
        }
    }
}