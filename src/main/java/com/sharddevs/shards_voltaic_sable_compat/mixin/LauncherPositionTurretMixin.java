package com.sharddevs.shards_voltaic_sable_compat.mixin;

import com.sharddevs.shards_voltaic_sable_compat.VoltaicSableCompat;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;

import ballistix.common.tile.turret.antimissile.TileTurretCIWS;
import ballistix.common.tile.turret.antimissile.TileTurretLaser;
import ballistix.common.tile.turret.antimissile.TileTurretRailgun;
import ballistix.common.tile.turret.antimissile.TileTurretSAM;

@Mixin({ TileTurretCIWS.class, TileTurretLaser.class,
        TileTurretRailgun.class, TileTurretSAM.class })
public abstract class LauncherPositionTurretMixin {

    @Inject(method = "getProjectileLaunchPosition", at = @At("RETURN"), cancellable = true)
    private void shards_voltaic_sable_compat$toWorldLaunchPos(CallbackInfoReturnable<Vec3> cir) {
        Vec3 local = cir.getReturnValue();
        if (local == null) {
            return;
        }
        BlockEntity self = (BlockEntity) (Object) this;
        SubLevel sub = Sable.HELPER.getContaining(self);
        if (sub != null) {
            Vec3 world = sub.logicalPose().transformPosition(local);
            cir.setReturnValue(world);
            VoltaicSableCompat.LOGGER.debug(
                    "[LAUNCH-POS] Turret launch pos {} -> {} (sub-level)", local, world);
        }
    }
}