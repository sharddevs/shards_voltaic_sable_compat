package com.sharddevs.shards_voltaic_sable_compat.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ShardsVscMixinPlugin implements IMixinConfigPlugin {

    /** Resolved once at plugin load — the mod list does not change afterwards. */
    private static final boolean BALLISTIX_LOADED =
            LoadingModList.get().getModFileById("ballistix") != null;

    /** Fully-qualified names of the mixins that require Ballistix. */
    private static final Set<String> BALLISTIX_GATED = Set.of(
            "com.sharddevs.shards_voltaic_sable_compat.mixin.ControlPanelRangeMixin",
            "com.sharddevs.shards_voltaic_sable_compat.mixin.LauncherPositionTurretMixin",
            "com.sharddevs.shards_voltaic_sable_compat.mixin.MissileLauncherPositionMixin"
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (BALLISTIX_GATED.contains(mixinClassName)) {
            return BALLISTIX_LOADED;
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
        // no-op
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // no-op
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }
}
