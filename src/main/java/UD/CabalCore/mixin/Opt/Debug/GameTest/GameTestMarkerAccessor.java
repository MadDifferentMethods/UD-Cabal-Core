package UD.CabalCore.mixin.Opt.Debug.GameTest;

import net.minecraft.client.renderer.debug.GameTestDebugRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GameTestDebugRenderer.class)
public interface GameTestMarkerAccessor {
    @Accessor("markers")
    Map<BlockPos, ?> cc$getMarkers();
}