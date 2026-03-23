package UD.CabalCore.mixin;

import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MusicManager.class)
public abstract class BGMS {

    @Shadow private int nextSongDelay;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cc$killMusicTick(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true)
    private void cc$killStartPlaying(Music music, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "stopPlaying", at = @At("HEAD"), cancellable = true)
    private void cc$killStopPlaying(CallbackInfo ci) {
        this.nextSongDelay = Integer.MAX_VALUE;
        ci.cancel();
    }

    @Inject(method = "isPlayingMusic", at = @At("HEAD"), cancellable = true)
    private void cc$forceNotPlaying(Music music, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}