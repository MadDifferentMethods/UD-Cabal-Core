package UD.CabalCore.mixin.Fixes.Boat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class BoatUse {

    @Shadow @Final
    protected Minecraft minecraft;

    @Shadow
    private boolean handsBusy;

    @Unique
    private static boolean cc$canUseFood(LocalPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !stack.isEdible()) {
            return false;
        }

        FoodProperties food = stack.getFoodProperties(player);
        if (food == null) {
            return false;
        }

        return player.canEat(food.canAlwaysEat());
    }

    @Unique
    private static boolean cc$isBoatAllowedUseItem(LocalPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (cc$canUseFood(player, stack)) {
            return true;
        }

        return stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem;
    }

    @Unique
    private static boolean cc$hasBoatAllowedUseItem(LocalPlayer player) {
        return cc$isBoatAllowedUseItem(player, player.getItemInHand(InteractionHand.MAIN_HAND))
                || cc$isBoatAllowedUseItem(player, player.getItemInHand(InteractionHand.OFF_HAND));
    }

    @Inject(method = "rideTick", at = @At("TAIL"))
    private void cc$allowBoatUseWhileRowing(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        if (!(self.getVehicle() instanceof Boat)) {
            return;
        }

        if (!this.minecraft.options.keyUse.isDown()) {
            return;
        }

        if (!cc$hasBoatAllowedUseItem(self)) {
            return;
        }

        // Vanilla marks handsBusy while rowing a boat if movement keys are held.
        // Clearing it here preserves rowing but allows the normal use-item path
        // to begin for food and ranged weapons.
        this.handsBusy = false;
    }
}