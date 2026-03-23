package UD.CabalCore.mixin.LangOp;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontSet.class)
public class ZalgoInfection {

    @Shadow @Final
    private BakedGlyph missingGlyph;

    @Unique
    private static boolean cc$isZalgo(int codePoint) {
        return codePoint >= 0x0300 && codePoint <= 0x036F;
    }

    @Unique
    private static final GlyphInfo CC_ZERO_ADVANCE_ZALGO = new GlyphInfo.SpaceGlyphInfo() {
        @Override
        public float getAdvance() {
            return 0.0F;
        }
    };

    @Inject(method = "getGlyphInfo", at = @At("HEAD"), cancellable = true)
    private void cc$zalgoZeroAdvance(int codePoint, boolean validateAdvance, CallbackInfoReturnable<GlyphInfo> cir) {
        if (cc$isZalgo(codePoint)) {
            cir.setReturnValue(CC_ZERO_ADVANCE_ZALGO);
        }
    }

    @Inject(method = "computeGlyphInfo", at = @At("HEAD"), cancellable = true, require = 0)
    private void cc$zalgoZeroAdvanceEarly(int codePoint, CallbackInfoReturnable<Object> cir) {
        if (cc$isZalgo(codePoint)) {
            cir.setReturnValue(CC_ZERO_ADVANCE_ZALGO);
        }
    }
}