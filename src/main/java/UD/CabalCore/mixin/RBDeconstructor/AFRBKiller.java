package UD.CabalCore.mixin.RBDeconstructor;

import UD.CabalCore.debloater.DeadFRBCore;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceScreen.class)
public abstract class AFRBKiller {

    @Shadow @Final @Mutable
    public AbstractFurnaceRecipeBookComponent recipeBookComponent;

    @Unique
    private static final AbstractFurnaceRecipeBookComponent CC_DEAD_FURNACE_RB = new DeadFRBCore();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cc$replaceRecipeBookComponent(CallbackInfo ci) {
        this.recipeBookComponent = CC_DEAD_FURNACE_RB;
    }
}