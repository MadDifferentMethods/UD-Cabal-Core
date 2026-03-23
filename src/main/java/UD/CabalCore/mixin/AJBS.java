package UD.CabalCore.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ControlsScreen.class)
public class AJBS {

    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;createButton(Lnet/minecraft/client/Options;III)Lnet/minecraft/client/gui/components/AbstractWidget;"
            )
    )
    private AbstractWidget cc$killAutoJumpButton(
            OptionInstance<?> option,
            Options options,
            int x,
            int y,
            int width
    ) {
        if (option == options.autoJump()) {
            return new Button(-10000, -10000, 0, 0, Component.empty(), b -> {});
        }

        return option.createButton(options, x, y, width);
    }
}