package UD.CabalCore.mixin.LangOp;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(TitleScreen.class)
public class TitleScreenLangKiller {

    @Redirect(
            method = "init()V",
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=narrator.button.language"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
                    ordinal = 0
            )
    )
    private GuiEventListener cc$blockMainMenuLanguageButton(TitleScreen instance, GuiEventListener listener) {
        return null;
    }
}