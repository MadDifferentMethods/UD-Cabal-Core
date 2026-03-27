package UD.CabalCore.mixin.Opt.Lightmap;

import UD.CabalCore.mixin.Opt.Flags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class LightmapInvalidator {

    @Shadow private boolean updateLightTexture;
    @Shadow private float blockLightRedFlicker;

    @Shadow @Final private GameRenderer renderer;
    @Shadow @Final private Minecraft minecraft;

    @Shadow protected abstract float getDarknessGamma(float partialTick);
    @Shadow protected abstract float calculateDarknessScale(LivingEntity entity, float darknessGamma, float partialTick);

    @Unique private boolean cc$lightmapStateInit;

    @Unique private int cc$lastEffectsId;
    @Unique private int cc$lastDimensionTypeId;
    @Unique private int cc$lastAmbientLight;
    @Unique private boolean cc$lastForceBright;

    @Unique private int cc$lastSkyDarken;
    @Unique private boolean cc$lastSkyFlash;

    @Unique private int cc$lastDarknessScaleOption;
    @Unique private int cc$lastDarknessGamma;
    @Unique private int cc$lastDarknessPulse;

    @Unique private int cc$lastWaterVision;
    @Unique private int cc$lastVisionBoost;

    @Unique private int cc$lastDarkenWorldAmount;
    @Unique private int cc$lastGamma;

    @Unique private int cc$lastBlockLightRedFlicker;

    @Unique
    private static int cc$q(float value, float scale) {
        return Math.round(value * scale);
    }

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void cc$invalidateLightmap(float partialTick, CallbackInfo ci) {
        if (!Flags.LIGHTMAP_INVALIDATOR) {
            return;
        }

        if (!this.updateLightTexture) {
            return;
        }

        ClientLevel level = this.minecraft.level;
        if (level == null || this.minecraft.player == null) {
            return;
        }

        DimensionType dimensionType = level.dimensionType();

        int effectsId = System.identityHashCode(level.effects());
        int dimensionTypeId = System.identityHashCode(dimensionType);
        int ambientLight = cc$q(dimensionType.ambientLight(), 4096.0F);
        boolean forceBright = level.effects().forceBrightLightmap();

        float skyDarken = level.getSkyDarken(1.0F);
        boolean skyFlash = level.getSkyFlashTime() > 0;

        float darknessScaleOption = ((Double) this.minecraft.options.darknessEffectScale().get()).floatValue();
        float darknessGamma = this.getDarknessGamma(partialTick) * darknessScaleOption;
        float darknessPulse = this.calculateDarknessScale(this.minecraft.player, darknessGamma, partialTick) * darknessScaleOption;

        float waterVision = this.minecraft.player.getWaterVision();
        float visionBoost;
        if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
            visionBoost = GameRenderer.getNightVisionScale(this.minecraft.player, partialTick);
        } else if (waterVision > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
            visionBoost = waterVision;
        } else {
            visionBoost = 0.0F;
        }

        float darkenWorldAmount = this.renderer.getDarkenWorldAmount(partialTick);
        float gamma = ((Double) this.minecraft.options.gamma().get()).floatValue();
        float flicker = this.blockLightRedFlicker;

        int qSkyDarken = cc$q(skyDarken, 256.0F);
        int qDarknessScaleOption = cc$q(darknessScaleOption, 2048.0F);
        int qDarknessGamma = cc$q(darknessGamma, 2048.0F);
        int qDarknessPulse = cc$q(darknessPulse, 1024.0F);
        int qWaterVision = cc$q(waterVision, 1024.0F);
        int qVisionBoost = cc$q(visionBoost, 1024.0F);
        int qDarkenWorldAmount = cc$q(darkenWorldAmount, 1024.0F);
        int qGamma = cc$q(gamma, 2048.0F);

        int qFlicker = cc$q(flicker, 64.0F);

        if (this.cc$lightmapStateInit
                && this.cc$lastEffectsId == effectsId
                && this.cc$lastDimensionTypeId == dimensionTypeId
                && this.cc$lastAmbientLight == ambientLight
                && this.cc$lastForceBright == forceBright
                && this.cc$lastSkyDarken == qSkyDarken
                && this.cc$lastSkyFlash == skyFlash
                && this.cc$lastDarknessScaleOption == qDarknessScaleOption
                && this.cc$lastDarknessGamma == qDarknessGamma
                && this.cc$lastDarknessPulse == qDarknessPulse
                && this.cc$lastWaterVision == qWaterVision
                && this.cc$lastVisionBoost == qVisionBoost
                && this.cc$lastDarkenWorldAmount == qDarkenWorldAmount
                && this.cc$lastGamma == qGamma) {
            this.updateLightTexture = false;
            ci.cancel();
            return;
        }

        this.cc$lightmapStateInit = true;

        this.cc$lastEffectsId = effectsId;
        this.cc$lastDimensionTypeId = dimensionTypeId;
        this.cc$lastAmbientLight = ambientLight;
        this.cc$lastForceBright = forceBright;

        this.cc$lastSkyDarken = qSkyDarken;
        this.cc$lastSkyFlash = skyFlash;

        this.cc$lastDarknessScaleOption = qDarknessScaleOption;
        this.cc$lastDarknessGamma = qDarknessGamma;
        this.cc$lastDarknessPulse = qDarknessPulse;

        this.cc$lastWaterVision = qWaterVision;
        this.cc$lastVisionBoost = qVisionBoost;

        this.cc$lastDarkenWorldAmount = qDarkenWorldAmount;
        this.cc$lastGamma = qGamma;

        this.cc$lastBlockLightRedFlicker = qFlicker;
    }
}