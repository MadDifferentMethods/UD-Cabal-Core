package UD.CabalCore.mixin.Opt.Tutoriel;

import UD.CabalCore.mixin.Opt.Flags;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Tutorial.class)
public abstract class TutNull {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cc$killTick(CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void cc$killStart(CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "stop", at = @At("HEAD"), cancellable = true)
    private void cc$killStop(CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "setStep", at = @At("HEAD"), cancellable = true)
    private void cc$killSetStep(TutorialSteps step, CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "addTimedToast", at = @At("HEAD"), cancellable = true)
    private void cc$killTimedToastAdd(TutorialToast toast, int ticks, CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "removeTimedToast", at = @At("HEAD"), cancellable = true)
    private void cc$killTimedToastRemove(TutorialToast toast, CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "onInput", at = @At("HEAD"), cancellable = true)
    private void cc$killInput(Input input, CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouse", at = @At("HEAD"), cancellable = true)
    private void cc$killMouse(double dx, double dy, CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "onLookAt", at = @At("HEAD"), cancellable = true)
    private void cc$killLookAt(@Nullable ClientLevel level, @Nullable HitResult hitResult, CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "onDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void cc$killDestroyBlock(ClientLevel level, BlockPos pos, BlockState state, float progress, CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "onOpenInventory", at = @At("HEAD"), cancellable = true)
    private void cc$killOpenInventory(CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "onGetItem", at = @At("HEAD"), cancellable = true)
    private void cc$killGetItem(ItemStack stack, CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }

    @Inject(method = "onInventoryAction", at = @At("HEAD"), cancellable = true)
    private void cc$killInventoryAction(ItemStack carried, ItemStack slotStack, ClickAction clickAction, CallbackInfo ci) {
        if (Flags.TUTORIAL_NULLER) {
            ci.cancel();
        }
    }
}