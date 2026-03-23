package UD.CabalCore.mixin.RBDeconstructor;

import UD.CabalCore.debloater.DeadRBCore;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingScreen.class)
public class CRBKiller {

    @Unique
    private static final RecipeBookComponent CC_DEAD_RECIPE_BOOK = new DeadRBCore();

    @Redirect(
            method = "<init>(Lnet/minecraft/world/inventory/CraftingMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;)V",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/client/gui/screens/recipebook/RecipeBookComponent"
            )
    )
    private RecipeBookComponent cc$deadRecipeBook() {
        return CC_DEAD_RECIPE_BOOK;
    }
}