package UD.CabalCore.mixin.UnReport;

import net.minecraft.client.multiplayer.chat.RollingMemoryChatLog;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ReportingContext.class)
public class RepDecontexter {

    // Redirect the 1024-slot buffer allocation to size 1.
    // The object still needs to exist since ReportingContext is a record,
    // but we're not storing 1024 dead LoggedChatEvent slots in memory.
    @Redirect(
            method = "create",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/client/multiplayer/chat/RollingMemoryChatLog"
            )
    )
    private static RollingMemoryChatLog shrinkChatLog(int capacity) {
        return new RollingMemoryChatLog(1);
    }
}