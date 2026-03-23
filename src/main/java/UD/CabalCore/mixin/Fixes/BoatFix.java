package UD.CabalCore.mixin.Fixes;

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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatFix extends Entity {

    protected BoatFix(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    private Boat.Status getStatus() {
        throw new AssertionError();
    }

    @Inject(method = "checkFallDamage", at = @At("HEAD"), cancellable = true)
    private void cc$forceLandImpactFix(
            double y,
            boolean onGround,
            BlockState state,
            BlockPos pos,
            CallbackInfo ci
    ) {
        Boat self = (Boat) (Object) this;

        if (!onGround) {
            return;
        }

        if (this.fallDistance <= 3.0F) {
            return;
        }

        // Solid landing check that works in 1.19.2 mappings
        if (state.isAir() || state.getCollisionShape(this.level, pos).isEmpty()) {
            return;
        }

        // If vanilla already sees this as ON_LAND, let vanilla handle it
        if (this.getStatus() == Boat.Status.ON_LAND) {
            return;
        }

        // Bug case: solid landing, but boat status is not ON_LAND,
        // so vanilla would wrongly reset fall distance and skip break/damage.
        self.causeFallDamage(this.fallDistance, 1.0F, DamageSource.FALL);

        if (!this.level.isClientSide && !this.isRemoved()) {
            this.kill();

            if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                for (int i = 0; i < 3; ++i) {
                    self.spawnAtLocation(self.getBoatType().getPlanks());
                }

                for (int i = 0; i < 2; ++i) {
                    self.spawnAtLocation(Items.STICK);
                }
            }
        }

        this.resetFallDistance();
        ci.cancel();
    }
}