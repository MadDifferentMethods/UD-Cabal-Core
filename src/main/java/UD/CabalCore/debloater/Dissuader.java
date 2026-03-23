package UD.CabalCore.debloater;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.SharedSuggestionProvider;

public class Dissuader extends CommandSuggestions {

    public Dissuader(
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
        super(
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

    @Override
    public void setAllowSuggestions(boolean value) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double amount) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void showSuggestions(boolean narrateFirst) {
    }

    @Override
    public void hide() {
    }

    @Override
    public void updateCommandInfo() {
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY) {
    }

    @Override
    public boolean renderSuggestions(PoseStack stack, int mouseX, int mouseY) {
        return false;
    }

    @Override
    public void renderUsage(PoseStack stack) {
    }

    @Override
    public String getNarrationMessage() {
        return "";
    }

    @Override
    public ParseResults<SharedSuggestionProvider> getCurrentContext() {
        return null;
    }

    @Override
    public CommandNode<SharedSuggestionProvider> getNodeAt(int cursor) {
        return null;
    }
}