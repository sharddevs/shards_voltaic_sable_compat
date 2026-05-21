package com.trevorgolden.voltaic_sable_compat.mixin;

import com.trevorgolden.voltaic_sable_compat.VoltaicSableCompat;

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

/**
 * Ballistix turrets compute getProjectileLaunchPosition() from getBlockPos().
 * On a Sable-assembled airship that BlockPos is the hidden sub-level coord
 * (~20M), so projectile spawn, fire-control distance checks and raycasts all
 * operate in sub-level space and the turret effectively can't fire usefully.
 *
 * We transform the returned launch position to real-world coordinates. Every
 * position-dependent turret behaviour routes through this one method, so
 * fixing the return value fixes spawn + targeting + raycast together.
 *
 * Identity transform when the turret is not in a sub-level (ground turret).
 */
@Mixin({ TileTurretCIWS.class, TileTurretLaser.class,
        TileTurretRailgun.class, TileTurretSAM.class })
public abstract class LauncherPositionTurretMixin {

    @Inject(method = "getProjectileLaunchPosition", at = @At("RETURN"), cancellable = true)
    private void voltaic_sable_compat$toWorldLaunchPos(CallbackInfoReturnable<Vec3> cir) {
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