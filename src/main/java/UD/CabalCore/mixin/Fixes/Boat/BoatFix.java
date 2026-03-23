package UD.CabalCore.mixin.Fixes.Boat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;

@Mixin(Boat.class)
public abstract class BoatFix extends Entity {

    protected BoatFix(EntityType<?> type, Level level) {
        super(type, level);
    }

    /**
     * @author DECAY
     * @reason Actually fixes the boat behavior.
     */
    @Overwrite
    protected void checkFallDamage(
            double y,
            boolean onGround,
            @Nonnull BlockState state,
            @Nonnull BlockPos pos
    ) {
        Boat self = (Boat) (Object) this;

        if (this.isPassenger()) {
            return;
        }

        if (onGround) {
            if (this.fallDistance > 3.0F) {
                boolean solidLanding = !state.isAir()
                        && !state.getCollisionShape(this.level, pos).isEmpty();

                if (solidLanding) {
                    for (Entity passenger : self.getPassengers()) {
                        passenger.causeFallDamage(this.fallDistance, 1.0F, DamageSource.FALL);
                    }

                    if (!this.level.isClientSide && !this.isRemoved()) {
                        this.kill();

                        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            for (int i = 0; i < 3; ++i) {
                                this.spawnAtLocation(self.getBoatType().getPlanks());
                            }

                            for (int i = 0; i < 2; ++i) {
                                this.spawnAtLocation(Items.STICK);
                            }
                        }
                    }
                }
            }

            this.resetFallDistance();
        } else if (!self.canBoatInFluid(this.level.getFluidState(this.blockPosition().below())) && y < 0.0D) {
            this.fallDistance -= (float) y;
        }
    }
}