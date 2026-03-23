package UD.CabalCore.mixin.NarNuller;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class NarSuggestionBypass {

    @Redirect(
            method = "select",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getNarrator()Lnet/minecraft/client/GameNarrator;"
            )
    )
    private GameNarrator cc$skipSuggestionNarratorGetter(net.minecraft.client.Minecraft instance) {
        return null;
    }

    @Redirect(
            method = "select",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/GameNarrator;sayNow(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void cc$skipSuggestionNarration(GameNarrator narrator, Component component) {
    }
}