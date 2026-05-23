package com.sharddevs.shards_voltaic_sable_compat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Sable's ServerLevelPlot#destroyAllBlocks clears a plot with
 * Level#destroyBlock(pos, true). During Simulated disassembly the physics
 * mass tracker can fire it mid-moveBlocks, destroying plot blocks that
 * moveBlocks has already copied into the world -> surviving block + dropped
 * item = the wire dupe. Plot teardown must never drop; force drop = false.
 */
@Mixin(targets = "dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot", remap = false)
public class PlotDestroyNoDropMixin {

    @Redirect(
            method = "destroyAllBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"
            )
    )
    private boolean shards_voltaic_sable_compat$noDropOnPlotTeardown(Level level, BlockPos pos, boolean drop) {
        return level.destroyBlock(pos, false);
    }
}