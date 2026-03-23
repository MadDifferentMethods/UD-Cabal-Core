package UD.CabalCore.mixin.RBDeconstructor;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientRecipeBook.class)
public class CRBDismantler {

    @Inject(method = "setupCollections", at = @At("HEAD"), cancellable = true)
    private void cc$killSetupCollections(Iterable<Recipe<?>> recipes, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "getCollections", at = @At("HEAD"), cancellable = true)
    private void cc$emptyAllCollections(CallbackInfoReturnable<List<RecipeCollection>> cir) {
        cir.setReturnValue(ImmutableList.of());
    }

    @Inject(method = "getCollection", at = @At("HEAD"), cancellable = true)
    private void cc$emptyCollectionsByTab(RecipeBookCategories category, CallbackInfoReturnable<List<RecipeCollection>> cir) {
        cir.setReturnValue(ImmutableList.of());
    }
}