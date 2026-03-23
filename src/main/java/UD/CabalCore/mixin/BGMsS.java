package UD.CabalCore.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.VolumeSlider;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SoundOptionsScreen.class)
public class BGMsS {

    @Redirect(
            method = "init",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/client/gui/components/VolumeSlider"
            )
    )
    private VolumeSlider cc$handleVolumeSlider(
            Minecraft minecraft,
            int x,
            int y,
            SoundSource source,
            int width
    ) {
        if (source == SoundSource.MUSIC) {
            return new VolumeSlider(minecraft, -10000, -10000, SoundSource.MASTER, 0);
        }

        return new VolumeSlider(minecraft, x, y, source, width);
    }
}