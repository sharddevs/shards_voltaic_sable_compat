package com.sharddevs.shards_voltaic_sable_compat.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin config plugin for shards_voltaic_sable_compat.
 *
 * <p>Ballistix is an OPTIONAL dependency. Three mixins target Ballistix
 * classes and must not be applied when Ballistix is absent — otherwise mixin
 * tries to transform classes that do not exist, which fails class transform
 * and can take the whole mod down (or spam the log).
 *
 * <p>{@link #shouldApplyMixin} returns {@code false} for the Ballistix-gated
 * mixins when Ballistix is not in the mod list, so mixin skips them entirely
 * and never touches the missing classes. The Sable-side mixins
 * (SubnodeCascadeFix, SableMoveBlocksRefresh, PlotDestroyNoDrop) always apply.
 *
 * <p>The check uses {@link LoadingModList} rather than {@code ModList}: mixin
 * config plugins run very early in startup, before {@code ModList} is
 * populated, whereas {@code LoadingModList} is available at that stage.
 */
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
