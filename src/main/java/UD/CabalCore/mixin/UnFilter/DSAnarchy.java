package UD.CabalCore.mixin.UnFilter;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.network.TextFilterClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(DedicatedServer.class)
public abstract class DSAnarchy {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/TextFilterClient;createFromConfig(Ljava/lang/String;)Lnet/minecraft/server/network/TextFilterClient;"
            )
    )
    @Nullable
    private TextFilterClient killFilterClient(String config) {
        return null;
    }
}