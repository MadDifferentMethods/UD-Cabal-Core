package UD.CabalCore.mixin.UnReport;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.datafixers.util.Unit;
import net.minecraft.client.multiplayer.chat.report.AbuseReportSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(AbuseReportSender.Services.class)
public class Debuser {

    // Master gate — every UI check and send path consults this first.
    // Returning false prevents the report button from appearing entirely.
    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true)
    private void disableReporting(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    // Safety net — if anything bypasses isEnabled(), the send silently no-ops.
    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void blockSend(UUID id, AbuseReport report, CallbackInfoReturnable<CompletableFuture<Unit>> cir) {
        cir.setReturnValue(CompletableFuture.completedFuture(Unit.INSTANCE));
    }
}