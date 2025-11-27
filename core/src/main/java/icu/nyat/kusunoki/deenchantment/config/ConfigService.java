package icu.nyat.kusunoki.deenchantment.config;

import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Coordinates all YAML-backed configuration files.
 */
public final class ConfigService {

    private final JavaPlugin plugin;
    private final PluginLogger logger;

    private PluginConfig pluginConfig;
    private MessageConfig messageConfig;
    private CurseCatalog curseCatalog;
    private FileConfiguration cachedConfig;
    private FileConfiguration cachedMessages;
    private FileConfiguration cachedCurses;

    public ConfigService(final JavaPlugin plugin, final PluginLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void load() {
        plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.cachedConfig = copyConfiguration(plugin.getConfig());
    this.cachedMessages = load("messages.yml");
    this.cachedCurses = loadDeEnchantments();
        this.pluginConfig = PluginConfig.from(cachedConfig);
        this.messageConfig = new MessageConfig(cachedMessages);
        this.curseCatalog = CurseCatalog.from(cachedCurses);
    }

    public boolean migrateLegacyConfig(final File legacyFile) {
        if (!legacyFile.exists()) {
            return false;
        }
        final YamlConfiguration legacy = YamlConfiguration.loadConfiguration(legacyFile);
        final FileConfiguration config = ensureConfig();
        config.set("anvil", legacy.getBoolean("Anvil", true));
        config.set("grindstone", legacy.getBoolean("Grindstone", true));
        config.set("enchant", legacy.getBoolean("EnchantTable", true));
        config.set("chestLoot", legacy.getBoolean("Chest", true));
        config.set("spawn", legacy.getBoolean("Mobs", true));
        config.set("trade", legacy.getBoolean("Villager", true));
        config.set("fishing", legacy.getBoolean("Fishing", true));
        config.set("reward", legacy.getBoolean("Reward", true));
        config.set("levelUnlimited", legacy.getBoolean("LevelUnlimited", false));
        config.set("tooExpensive", legacy.getBoolean("AllowTooExpensive", false));
        config.set("cleanConsole", legacy.getBoolean("CleanConsole", false));
        config.set("allowDescription", legacy.getBoolean("AllowDescription", config.getBoolean("allowDescription", true)));
        config.set("lorePosition", legacy.getInt("LorePosition", config.getInt("lorePosition", 0)));
        config.set("enchantsPermission", legacy.getBoolean("EnchantPermission", config.getBoolean("enchantsPermission", false)));
        config.set("debug", legacy.getBoolean("Debug", config.getBoolean("debug", false)));
        saveConfiguration(config, new File(plugin.getDataFolder(), "config.yml"));

        final FileConfiguration messages = ensureMessages();
        final String legacyPrefix = legacy.getString("Prefix");
        if (legacyPrefix != null) {
            messages.set("prefix", legacyPrefix);
        }
        saveConfiguration(messages, new File(plugin.getDataFolder(), "messages.yml"));

        try {
            if (cachedConfig != null) {
                cachedConfig.loadFromString(config.saveToString());
            }
            if (cachedMessages != null) {
                cachedMessages.loadFromString(messages.saveToString());
            }
        } catch (final Exception exception) {
            logger.error("Failed to refresh cached configurations", exception);
        }
        reload();
        return true;
    }

    private FileConfiguration ensureConfig() {
        if (cachedConfig == null) {
            cachedConfig = copyConfiguration(plugin.getConfig());
        }
        return cachedConfig;
    }

    private FileConfiguration ensureMessages() {
        if (cachedMessages == null) {
            cachedMessages = load("messages.yml");
        }
        return cachedMessages;
    }

    private FileConfiguration load(final String fileName) {
        final File target = new File(plugin.getDataFolder(), fileName);
        if (!target.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(target);
    }

    private FileConfiguration loadDeEnchantments() {
        final File target = new File(plugin.getDataFolder(), "DeEnchantments.yml");
        if (!target.exists()) {
            migrateLegacyCurses(target);
        }
        if (!target.exists()) {
            plugin.saveResource("DeEnchantments.yml", false);
        }
        return YamlConfiguration.loadConfiguration(target);
    }

    private void migrateLegacyCurses(final File destination) {
        final File legacy = new File(plugin.getDataFolder(), "curses.yml");
        if (!legacy.exists()) {
            return;
        }
        if (legacy.renameTo(destination)) {
            logger.info("Migrated legacy curses.yml to DeEnchantments.yml");
            return;
        }
        try {
            java.nio.file.Files.copy(legacy.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied legacy curses.yml to DeEnchantments.yml");
        } catch (final IOException exception) {
            logger.error("Failed to migrate curses.yml, falling back to defaults", exception);
        }
    }

    private FileConfiguration copyConfiguration(final FileConfiguration source) {
        final YamlConfiguration copy = new YamlConfiguration();
        try {
            copy.loadFromString(source.saveToString());
        } catch (final Exception exception) {
            logger.error("Failed to copy configuration", exception);
        }
        return copy;
    }

    private void saveConfiguration(final FileConfiguration configuration, final File destination) {
        try {
            configuration.save(destination);
        } catch (final IOException exception) {
            logger.error("Failed to save configuration to " + destination.getName(), exception);
        }
    }

    public PluginConfig plugin() {
        return pluginConfig;
    }

    public MessageConfig messages() {
        return messageConfig;
    }

    public CurseCatalog curses() {
        return curseCatalog;
    }
}
