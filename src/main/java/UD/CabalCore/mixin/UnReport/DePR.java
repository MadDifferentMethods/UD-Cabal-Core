package UD.CabalCore.mixin.UnReport;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class DePR {

    @Inject(method = "createPauseMenu", at = @At("RETURN"))
    private void removeReportButton(CallbackInfo ci) {
        ((Screen)(Object)this).renderables.removeIf(w ->
                w instanceof Button btn &&
                        btn.getMessage().equals(Component.translatable("menu.playerReporting"))
        );
    }
}