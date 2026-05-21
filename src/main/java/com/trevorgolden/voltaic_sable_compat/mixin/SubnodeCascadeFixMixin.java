package com.trevorgolden.voltaic_sable_compat.mixin;

import com.trevorgolden.voltaic_sable_compat.VoltaicSableCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import voltaic.common.block.BlockMultiSubnode;

@Mixin(BlockMultiSubnode.class)
public abstract class SubnodeCascadeFixMixin {

    @Inject(
        method = "onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void voltaic_sable_compat$preventCascadeDuringMove(
        BlockState state,
        Level level,
        BlockPos pos,
        BlockState newState,
        boolean isMoving,
        CallbackInfo ci
    ) {
        if (isMoving) {
            VoltaicSableCompat.LOGGER.debug("[CASCADE-SUPPRESS] Suppressing subnode cascade at {} during Sable move", pos);
            // Skip the Voltaic cascade entirely — block is being moved, not destroyed.
            // Vanilla setBlockState already handles cleanup of the old block.
            ci.cancel();
        }
    }
}
