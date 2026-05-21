package com.trevorgolden.voltaic_sable_compat.mixin;

import com.trevorgolden.voltaic_sable_compat.VoltaicSableCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class ShatterBlocksDiagnosticMixin {

    @Inject(
        method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD")
    )
    private static void voltaic_sable_compat$tracePopResource(
        Level level, BlockPos pos, ItemStack stack, CallbackInfo ci
    ) {
        // Only log drops for ballistix/voltaic/electrodynamics
        String regName = stack.getItem().toString();
        if (!regName.contains("ballistix") && !regName.contains("voltaic") && !regName.contains("electrodynamics")) {
            return;
        }
        
        VoltaicSableCompat.LOGGER.info("[DROP-TRACE] popResource called for {} at {}", regName, pos);
        VoltaicSableCompat.LOGGER.info("[DROP-TRACE] Stack trace:");
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 1; i < Math.min(trace.length, 25); i++) {
            VoltaicSableCompat.LOGGER.info("[DROP-TRACE]   {}", trace[i].toString());
        }
    }
}
