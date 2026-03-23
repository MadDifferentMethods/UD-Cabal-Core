package UD.CabalCore.mixin.LangOp;

import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.font.FontManager$1")
public class FontUnprovider {

    @Redirect(
            method = "prepare",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/providers/GlyphProviderBuilder;create(Lnet/minecraft/server/packs/resources/ResourceManager;)Lcom/mojang/blaze3d/font/GlyphProvider;"
            )
    )
    private GlyphProvider cc$filterProviders(GlyphProviderBuilder builder, ResourceManager resourceManager) {
        String name = builder.getClass().getName();

        if (name.contains("BitmapProvider") || name.contains("SpaceProvider")) {
            return builder.create(resourceManager);
        }
        return null;
    }
}