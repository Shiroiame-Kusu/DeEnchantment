package icu.nyat.kusunoki.deenchantment.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Strongly typed view of {@code config.yml}.
 */
public final class PluginConfig {

    private final boolean enableAnvil;
    private final boolean enableGrindstone;
    private final boolean enableEnchanting;
    private final boolean enableChestLoot;
    private final boolean enableSpawn;
    private final boolean enableTrade;
    private final boolean enableFishing;
    private final boolean enableRewardDrops;
    private final boolean allowLevelUnlimited;
    private final boolean ignoreTooExpensive;
    private final boolean cleanConsole;
    private final boolean showDescription;
    private final int lorePosition;
    private final boolean requirePermissions;
    private final boolean debugLogging;

    private PluginConfig(
            final boolean enableAnvil,
            final boolean enableGrindstone,
            final boolean enableEnchanting,
            final boolean enableChestLoot,
            final boolean enableSpawn,
            final boolean enableTrade,
            final boolean enableFishing,
            final boolean enableRewardDrops,
            final boolean allowLevelUnlimited,
            final boolean ignoreTooExpensive,
            final boolean cleanConsole,
            final boolean showDescription,
            final int lorePosition,
            final boolean requirePermissions,
            final boolean debugLogging
    ) {
        this.enableAnvil = enableAnvil;
        this.enableGrindstone = enableGrindstone;
        this.enableEnchanting = enableEnchanting;
        this.enableChestLoot = enableChestLoot;
        this.enableSpawn = enableSpawn;
        this.enableTrade = enableTrade;
        this.enableFishing = enableFishing;
        this.enableRewardDrops = enableRewardDrops;
        this.allowLevelUnlimited = allowLevelUnlimited;
        this.ignoreTooExpensive = ignoreTooExpensive;
        this.cleanConsole = cleanConsole;
        this.showDescription = showDescription;
        this.lorePosition = lorePosition;
        this.requirePermissions = requirePermissions;
        this.debugLogging = debugLogging;
    }

    public static PluginConfig from(final FileConfiguration config) {
        return new PluginConfig(
                config.getBoolean("anvil", true),
                config.getBoolean("grindstone", true),
                config.getBoolean("enchant", true),
                config.getBoolean("chestLoot", true),
                config.getBoolean("spawn", true),
                config.getBoolean("trade", true),
                config.getBoolean("fishing", true),
                config.getBoolean("reward", true),
                config.getBoolean("levelUnlimited", false),
                config.getBoolean("tooExpensive", false),
                config.getBoolean("cleanConsole", false),
                config.getBoolean("allowDescription", false),
                config.getInt("lorePosition", 0),
                config.getBoolean("enchantsPermission", false),
                config.getBoolean("debug", false)
        );
    }

    public boolean isEnableAnvil() {
        return enableAnvil;
    }

    public boolean isEnableGrindstone() {
        return enableGrindstone;
    }

    public boolean isEnableEnchanting() {
        return enableEnchanting;
    }

    public boolean isEnableChestLoot() {
        return enableChestLoot;
    }

    public boolean isEnableSpawn() {
        return enableSpawn;
    }

    public boolean isEnableTrade() {
        return enableTrade;
    }

    public boolean isEnableFishing() {
        return enableFishing;
    }

    public boolean isEnableRewardDrops() {
        return enableRewardDrops;
    }

    public boolean isAllowLevelUnlimited() {
        return allowLevelUnlimited;
    }

    public boolean isIgnoreTooExpensive() {
        return ignoreTooExpensive;
    }

    public boolean isCleanConsole() {
        return cleanConsole;
    }

    public boolean isShowDescription() {
        return showDescription;
    }

    public int getLorePosition() {
        return lorePosition;
    }

    public boolean isRequirePermissions() {
        return requirePermissions;
    }

    public boolean isDebugLogging() {
        return debugLogging;
    }
}
