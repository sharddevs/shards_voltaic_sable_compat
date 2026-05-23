package com.sharddevs.shards_voltaic_sable_compat.mixin;

import com.sharddevs.shards_voltaic_sable_compat.VoltaicSableCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;

import ballistix.common.tile.silo.TileLauncherControlPanelT1;
import ballistix.common.tile.TileVerticalLaunchSilo;
@Mixin({ TileLauncherControlPanelT1.class, TileVerticalLaunchSilo.class })
public abstract class ControlPanelRangeMixin {

    @Redirect(
            method = "tickServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lballistix/common/tile/silo/TileLauncherControlPanelT1;calculateDistance(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)D"
            ),
            remap = false
    )
    private double shards_voltaic_sable_compat$worldSpaceDistance(BlockPos fromPos, BlockPos toPos) {
        try {
            BlockEntity self = (BlockEntity) (Object) this;
            SubLevel sub = Sable.HELPER.getContaining(self);
            if (sub != null) {
                Vec3 local = new Vec3(fromPos.getX() + 0.5, fromPos.getY() + 0.5, fromPos.getZ() + 0.5);
                BlockPos worldFrom = BlockPos.containing(sub.logicalPose().transformPosition(local));
                VoltaicSableCompat.LOGGER.debug(
                        "[LAUNCH-POS] CP range: {} -> {} (sub-level)", fromPos, worldFrom);
                fromPos = worldFrom;
            }
        } catch (Throwable e) {
            VoltaicSableCompat.LOGGER.error("[LAUNCH-POS] CP range transform failed", e);
        }
        return TileLauncherControlPanelT1.calculateDistance(fromPos, toPos);
    }
}