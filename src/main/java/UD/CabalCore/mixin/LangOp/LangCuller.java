package UD.CabalCore.mixin.LangOp;

import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.stream.Stream;

@Mixin(LanguageManager.class)
public class LangCuller {

    @Inject(method = "extractLanguages", at = @At("HEAD"), cancellable = true)
    private static void cc$extractOnlyEnglish(Stream<PackResources> packs,
                                              CallbackInfoReturnable<Map<String, LanguageInfo>> cir) {
        cir.setReturnValue(Map.of(
                "en_us",
                new LanguageInfo("en_us", "US", "English", false)
        ));
    }
}