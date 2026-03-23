package UD.CabalCore.mixin.Dissuader;

import UD.CabalCore.debloater.Dissuader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public class Dissuasion {

    @Redirect(
            method = "init",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/client/gui/components/CommandSuggestions"
            )
    )
    private CommandSuggestions cc$killSuggestions(
            Minecraft mc,
            Screen screen,
            EditBox input,
            Font font,
            boolean commandsOnly,
            boolean onlyShowIfCursorPastError,
            int lineStartOffset,
            int suggestionLineLimit,
            boolean anchorToBottom,
            int fillColor
    ) {
        return new Dissuader(
                mc,
                screen,
                input,
                font,
                commandsOnly,
                onlyShowIfCursorPastError,
                lineStartOffset,
                suggestionLineLimit,
                anchorToBottom,
                fillColor
        );
    }
}