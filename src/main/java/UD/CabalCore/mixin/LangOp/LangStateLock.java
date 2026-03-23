package UD.CabalCore.mixin.LangOp;

import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LanguageManager.class)
public abstract class LangStateLock {

    @Shadow private String currentCode;

    @Shadow private LanguageInfo currentLanguage;

    @Shadow private Map<String, LanguageInfo> languages;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cc$forceCtorLang(String code, CallbackInfo ci) {
        this.currentCode = "en_us";
        this.currentLanguage = this.languages.getOrDefault("en_us", null);
    }

    @Inject(method = "setSelected", at = @At("HEAD"), cancellable = true)
    private void cc$forceSelected(LanguageInfo lang, CallbackInfo ci) {
        this.currentCode = "en_us";
        this.currentLanguage = this.languages.getOrDefault("en_us", lang);
        ci.cancel();
    }

    @Inject(method = "getSelected", at = @At("HEAD"), cancellable = true)
    private void cc$forceGetSelected(CallbackInfoReturnable<LanguageInfo> cir) {
        cir.setReturnValue(this.languages.getOrDefault("en_us", this.currentLanguage));
    }

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void cc$forceReloadLang(net.minecraft.server.packs.resources.ResourceManager resourceManager, CallbackInfo ci) {
        this.currentCode = "en_us";
        this.currentLanguage = this.languages.getOrDefault("en_us", this.currentLanguage);
    }
}