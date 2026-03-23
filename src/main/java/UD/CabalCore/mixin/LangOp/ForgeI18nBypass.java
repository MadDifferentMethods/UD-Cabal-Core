package UD.CabalCore.mixin.LangOp;

import net.minecraftforge.common.ForgeI18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = ForgeI18n.class, remap = false)
public class ForgeI18nBypass {

    @Inject(method = "loadLanguageData", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cc$killLoadLanguageData(Map<String, String> properties, CallbackInfo ci) {
        ci.cancel();
    }
}