package UD.CabalCore.mixin.RBDeconstructor;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingScreen.class)
public class CraftingRBStrip {

    @Redirect(
            method = "init()V",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/client/gui/components/ImageButton"
            )
    )
    private ImageButton cc$killRecipeBookButtonCtor(
            int x,
            int y,
            int width,
            int height,
            int xTexStart,
            int yTexStart,
            int yDiffText,
            ResourceLocation texture,
            Button.OnPress onPress
    ) {
        return new ImageButton(
                -10000, -10000,
                0, 0,
                0, 0, 0,
                texture,
                b -> { }
        );
    }
}