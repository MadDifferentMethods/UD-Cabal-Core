package UD.CabalCore.mixin.UnFilter;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public abstract class SBdE {

    @Shadow @Final private Component[] filteredMessages;
    @Shadow @Final private Component[] messages;

    // 1. Intercept the setter.
    // When the game tries to set a filtered message, we force it to set the raw message instead.
    // This ensures filteredMessages always equals messages.
    @Inject(method = "setMessage(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void ignoreFilteredComponent(int line, Component raw, Component filtered, CallbackInfo ci) {
        this.messages[line] = raw;
        this.filteredMessages[line] = raw; // Force raw over filtered
        ci.cancel();
    }

    // 2. Intercept the getter.
    // Even if data exists in NBT, we force the game to return the raw message.
    @Inject(method = "getMessage", at = @At("HEAD"), cancellable = true)
    private void forceRawMessage(int line, boolean filtered, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(this.messages[line]);
    }
}