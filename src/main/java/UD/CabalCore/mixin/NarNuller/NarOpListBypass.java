package UD.CabalCore.mixin.NarNuller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(OptionsList.class)
public abstract class NarOpListBypass {

    @Shadow
    public abstract void addSmall(OptionInstance<?> first, @Nullable OptionInstance<?> second);

    @Inject(
            method = "addSmall([Lnet/minecraft/client/OptionInstance;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cc$filterNarratorOption(OptionInstance<?>[] options, CallbackInfo ci) {
        OptionInstance<?> narrator = Minecraft.getInstance().options.narrator();

        List<OptionInstance<?>> filtered = new ArrayList<>(options.length);
        for (OptionInstance<?> opt : options) {
            if (opt != narrator) {
                filtered.add(opt);
            }
        }

        for (int i = 0; i < filtered.size(); i += 2) {
            OptionInstance<?> first = filtered.get(i);
            OptionInstance<?> second = (i + 1 < filtered.size()) ? filtered.get(i + 1) : null;
            this.addSmall(first, second);
        }

        ci.cancel();
    }
}