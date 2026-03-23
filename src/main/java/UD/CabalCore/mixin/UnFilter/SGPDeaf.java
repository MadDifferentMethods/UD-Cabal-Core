package UD.CabalCore.mixin.UnFilter; //Server Game Packet Deafener

import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerGamePacketListenerImpl.class)
public class SGPDeaf {

    // 1. Kill string filtering (Chat, Command blocks, etc.)
    // Returns a completed future immediately with the raw text wrapped in pass-through.
    @Inject(method = "filterTextPacket(Ljava/lang/String;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
    private void bypassStringFilter(String raw, CallbackInfoReturnable<CompletableFuture<FilteredText>> cir) {
        cir.setReturnValue(CompletableFuture.completedFuture(FilteredText.passThrough(raw)));
    }

    // 2. Kill list filtering (Books, Signs)
    // Returns a completed future immediately with a list of pass-throughs.
    @Inject(method = "filterTextPacket(Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
    private void bypassListFilter(List<String> rawLines, CallbackInfoReturnable<CompletableFuture<List<FilteredText>>> cir) {
        // We perform the mapping here instantly, avoiding the TextFilter logic and wrapper allocations inside the original method.
        List<FilteredText> result = new java.util.ArrayList<>(rawLines.size());
        for (String line : rawLines) {
            result.add(FilteredText.passThrough(line));
        }
        cir.setReturnValue(CompletableFuture.completedFuture(result));
    }
}