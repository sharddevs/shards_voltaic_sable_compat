package com.sharddevs.shards_voltaic_sable_compat;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(VoltaicSableCompat.MODID)
public class VoltaicSableCompat {
    public static final String MODID = "shards_voltaic_sable_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VoltaicSableCompat() {
        LOGGER.info("Voltaic-Sable Compat loading. Server-side only.");
    }
}
