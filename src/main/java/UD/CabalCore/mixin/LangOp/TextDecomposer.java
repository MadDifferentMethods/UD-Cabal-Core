package UD.CabalCore.mixin.LangOp;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringDecomposer.class)
public class TextDecomposer {

    @Unique
    private static boolean cc$allowChar(char ch) {
        // printable ASCII
        if (ch >= 0x20 && ch <= 0x7E) return true;

        // section sign for formatting
        if (ch == 0x00A7) return true;

        // whitespace/control worth keeping
        if (ch == '\n' || ch == '\r' || ch == '\t') return true;

        // Zalgo combining marks
        if (ch >= 0x0300 && ch <= 0x036F) return true;

        return false;
    }

    @Inject(method = "feedChar", at = @At("HEAD"), cancellable = true)
    private static void cc$skipDeniedChars(Style style,
                                           FormattedCharSink sink,
                                           int index,
                                           char ch,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!cc$allowChar(ch)) {
            // true = continue iteration, but skip sending this char to the sink
            cir.setReturnValue(true);
        }
    }
}