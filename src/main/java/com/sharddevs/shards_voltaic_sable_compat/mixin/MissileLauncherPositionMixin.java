package com.sharddevs.shards_voltaic_sable_compat.mixin;

import com.sharddevs.shards_voltaic_sable_compat.VoltaicSableCompat;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;

import ballistix.common.tile.silo.TileLauncherPlatformT1;
import ballistix.common.tile.TileVerticalLaunchSilo;
@Mixin({ TileLauncherPlatformT1.class, TileVerticalLaunchSilo.class })
public abstract class MissileLauncherPositionMixin {

    @ModifyArgs(
            method = "launchMissile",
            at = @At(
                    value = "INVOKE",
                    target = "Lballistix/api/missile/virtual/VirtualMissile;<init>(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;FLballistix/api/missile/virtual/FlightPath;FFLnet/minecraft/core/BlockPos;ILballistix/api/blast/IBlast;IZ)V"
            ),
            remap = false
    )
    private void shards_voltaic_sable_compat$worldSpaceLaunch(Args args) {
        try {
            BlockEntity self = (BlockEntity) (Object) this;
            SubLevel sub = Sable.HELPER.getContaining(self);
            if (sub == null) {
                return; // ground silo -- no transform needed
            }
            Vec3 localSpawn = args.get(0);
            Vec3 worldSpawn = sub.logicalPose().transformPosition(localSpawn);
            args.set(0, worldSpawn);
            args.set(4, (float) worldSpawn.x);
            args.set(5, (float) worldSpawn.z);
            VoltaicSableCompat.LOGGER.debug(
                    "[LAUNCH-POS] Missile launch {} -> {} (sub-level)", localSpawn, worldSpawn);
        } catch (Throwable e) {
            VoltaicSableCompat.LOGGER.error("[LAUNCH-POS] transform failed", e);
        }
    }
}