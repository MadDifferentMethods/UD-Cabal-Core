package UD.CabalCore.mixin.NoMansRealm;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TitleScreen.class)
public class RBW {

    @Redirect(
            method = "createNormalMenuOptions(II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
                    ordinal = 2
            )
    )
    private GuiEventListener cc$blockRealmsButton(TitleScreen instance, GuiEventListener listener) {
        return listener;
    }
}