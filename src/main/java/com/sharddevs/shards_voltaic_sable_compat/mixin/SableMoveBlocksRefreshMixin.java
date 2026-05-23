package com.sharddevs.shards_voltaic_sable_compat.mixin;

import com.sharddevs.shards_voltaic_sable_compat.VoltaicSableCompat;

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
import voltaic.prefab.properties.variant.AbstractProperty;
import voltaic.prefab.tile.GenericTile;
import voltaic.prefab.tile.types.GenericRefreshingConnectTile;
import voltaic.prefab.utilities.Scheduler;

/**
 * Sable suppresses onPlace during moveBlocks, so Voltaic tiles never run their
 * post-placement refresh after a sub-level move.
 *
 * Two independent injections at the same setBlockState point:
 *
 *  - refreshCableAfterMove: refreshing-connect cables (wires/pipes). Rebuilds
 *    connection state + re-registers with the energy network. Requires the
 *    whole power system glued.
 *
 *  - resyncMachineAfterMove: machine tiles. After a move the machine's server
 *    state is intact, but its synced properties (ComponentElectrodynamic
 *    joules, frequency, target, etc -- all Voltaic SingleProperty values) are
 *    never re-sent to clients, because the move suppresses the normal sync
 *    trigger. The client GUI keeps showing stale values (observed: a moved
 *    control panel's energy bar reads 0 while the server has full charge and
 *    the launcher fires fine). We force every property dirty so Voltaic's
 *    PropertyManager re-syncs them to watching clients.
 *
 * The two are kept separate so the machine path cannot regress the proven
 * cable path.
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
    private static void shards_voltaic_sable_compat$refreshCableAfterMove(
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
            } catch (Throwable e) {
                VoltaicSableCompat.LOGGER.error(
                        "[CABLE-REFRESH] refresh failed at {}", targetPos, e);
            }
        });
    }

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
    private static void shards_voltaic_sable_compat$resyncMachineAfterMove(
            ServerLevel level, dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci,
            @Local(ordinal = 1) ServerLevel resultingLevel,
            @Local(ordinal = 2) BlockPos newPos,
            @Local(ordinal = 1) BlockState subLevelState
    ) {
        // Cables are handled by refreshCableAfterMove; this path is machines.
        if (subLevelState.getBlock() instanceof AbstractRefreshingConnectBlock) {
            return;
        }
        final ServerLevel targetLevel = resultingLevel;
        final BlockPos targetPos = newPos.immutable();
        // Defer one tick so the whole contraption finishes moving first.
        Scheduler.schedule(1, () -> {
            try {
                BlockEntity be = targetLevel.getBlockEntity(targetPos);
                if (!(be instanceof GenericTile tile) || tile.isRemoved()) {
                    return;
                }
                // Force every synced property dirty so Voltaic's PropertyManager
                // re-sends them to clients -- the move leaves the client GUI
                // showing stale values (energy bar, frequency, target, etc).
                for (AbstractProperty property : tile.getPropertyManager().getProperties()) {
                    property.forceDirtyForManager();
                }
            } catch (Throwable e) {
                VoltaicSableCompat.LOGGER.error(
                        "[MACHINE-RESYNC] resync failed at {}", targetPos, e);
            }
        });
    }
}