package UD.CabalCore.mixin.LangOp;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(OptionsScreen.class)
public class LangMenuKiller {

    @Redirect(
            method = "init()V",
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=options.language"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/OptionsScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
                    ordinal = 0
            )
    )
    private GuiEventListener cc$blockLanguageButton(OptionsScreen instance, GuiEventListener listener) {
        return null;
    }
}