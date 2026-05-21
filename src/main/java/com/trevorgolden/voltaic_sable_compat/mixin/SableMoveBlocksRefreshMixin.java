package com.trevorgolden.voltaic_sable_compat.mixin;

import com.trevorgolden.voltaic_sable_compat.VoltaicSableCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import voltaic.common.block.connect.AbstractRefreshingConnectBlock;
import voltaic.prefab.tile.types.GenericRefreshingConnectTile;
import voltaic.prefab.utilities.Scheduler;

/**
 * Sable suppresses onPlace during moveBlocks, so Voltaic refreshing cables
 * (BlockWire / BlockFluidPipe / BlockGasPipe) never rebuild connection state
 * or re-register with their network after a sub-level move.
 *
 * We inject after the per-block setBlockState (ordinal 0 = placement at the
 * new position) and, deferred one tick, refresh the cable: onPlace rebuilds
 * connection state, updateNetwork re-registers with the energy network,
 * sendBlockUpdated pushes the visual to clients.
 *
 * Known limitation at this baseline: wires do not always render visually
 * connected after assembly. Energy was observed flowing.
 */
@Mixin(targets = "dev.ryanhcode.sable.api.SubLevelAssemblyHelper")
public abstract class SableMoveBlocksRefreshMixin {

    @Inject(
            method = "moveBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private static void voltaic_sable_compat$refreshCableAfterMove(
            ServerLevel level, dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci,
            @Local(ordinal = 1) ServerLevel resultingLevel,
            @Local(ordinal = 2) BlockPos newPos,
            @Local(ordinal = 1) BlockState subLevelState
    ) {
        if (!(subLevelState.getBlock() instanceof AbstractRefreshingConnectBlock)) {
            return;
        }
        final ServerLevel targetLevel = resultingLevel;
        final BlockPos targetPos = newPos.immutable();
        // Defer one tick so the whole contraption finishes moving first.
        Scheduler.schedule(1, () -> {
            try {
                BlockEntity be = targetLevel.getBlockEntity(targetPos);
                if (!(be instanceof GenericRefreshingConnectTile<?, ?, ?> tile) || tile.isRemoved()) {
                    return;
                }
                BlockState st = targetLevel.getBlockState(targetPos);
                if (st.getBlock() instanceof AbstractRefreshingConnectBlock cableBlock) {
                    cableBlock.onPlace(st, targetLevel, targetPos, st, false);
                }
                tile.updateNetwork(Direction.values());
                targetLevel.sendBlockUpdated(targetPos, st, st, Block.UPDATE_CLIENTS);
                VoltaicSableCompat.LOGGER.info(
                        "[CABLE-REFRESH] Refreshed cable network at {} after Sable move", targetPos);
            } catch (Throwable e) {
                VoltaicSableCompat.LOGGER.error(
                        "[CABLE-REFRESH] refresh failed at {}", targetPos, e);
            }
        });
    }
}