package UD.CabalCore.mixin.Fixes.Boat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class Dismounter {

    @Unique
    private static final double DISMOUNT_CHANCE = 0.05;

    @Unique
    private static final int CHECK_INTERVAL = 40;
    @Inject(method = "tick", at = @At("TAIL"))

    private void cc$randomBoatDismount(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;

        // Only server-side
        Level level = self.level;
        if (level.isClientSide) {
            return;
        }

        // Only mobs (exclude players)
        if (!(self instanceof LivingEntity) || self instanceof Player) {
            return;
        }

        // Must be riding a boat
        if (!(self.getVehicle() instanceof Boat)) {
            return;
        }

        // Run check every few ticks, not every tick
        if (self.tickCount % CHECK_INTERVAL != 0) {
            return;
        }

        // Random chance
        if (level.random.nextDouble() < DISMOUNT_CHANCE) {
            self.stopRiding();
        }
    }
}