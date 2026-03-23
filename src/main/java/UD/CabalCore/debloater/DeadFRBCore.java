package UD.CabalCore.debloater;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DeadFRBCore extends AbstractFurnaceRecipeBookComponent {

    @Override
    protected Set<Item> getFuelItems() {
        return Collections.emptySet();
    }

    @Override
    public void tick() {
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void renderTooltip(PoseStack poseStack, int leftPos, int topPos, int mouseX, int mouseY) {
    }

    @Override
    public void renderGhostRecipe(PoseStack poseStack, int leftPos, int topPos, boolean highlight, float partialTick) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    @Override
    public boolean changeFocus(boolean forward) {
        return false;
    }

    @Override
    public void toggleVisibility() {
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void slotClicked(@Nullable Slot slot) {
    }

    @Override
    public void recipesUpdated() {
    }

    @Override
    public void recipesShown(List<Recipe<?>> recipes) {
    }

    @Override
    public void removed() {
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, int width, int height, int button) {
        return true;
    }
}