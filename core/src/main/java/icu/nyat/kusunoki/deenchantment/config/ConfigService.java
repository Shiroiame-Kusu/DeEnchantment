package icu.nyat.kusunoki.deenchantment.config;

import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Coordinates all YAML-backed configuration files.
 */
public final class ConfigService {

    private static final String DEFAULT_LANGUAGE = "zh_CN";

    private final JavaPlugin plugin;
    private final PluginLogger logger;

    private PluginConfig pluginConfig;
    private MessageConfig messageConfig;
    private LanguageConfig languageConfig;
    private CurseCatalog curseCatalog;
    private FileConfiguration cachedConfig;
    private FileConfiguration cachedLanguage;
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
        this.pluginConfig = PluginConfig.from(cachedConfig);
        this.cachedLanguage = loadLanguage();
        this.cachedCurses = loadDeEnchantments();
        // MessageConfig now uses language file for localized messages
        this.messageConfig = new MessageConfig(cachedLanguage);
        this.languageConfig = new LanguageConfig(cachedLanguage, pluginConfig.language());
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

        try {
            if (cachedConfig != null) {
                cachedConfig.loadFromString(config.saveToString());
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

    private FileConfiguration loadLanguage() {
        final String locale = cachedConfig.getString("language", DEFAULT_LANGUAGE);
        final String langPath = "lang/" + locale + ".yml";
        final File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        // Save default language files
        saveLanguageResource("lang/zh_CN.yml");
        saveLanguageResource("lang/en_US.yml");
        
        final File target = new File(plugin.getDataFolder(), langPath);
        if (!target.exists()) {
            // Fall back to default language if specified language file doesn't exist
            logger.info("Language file " + langPath + " not found, falling back to " + DEFAULT_LANGUAGE);
            final File fallback = new File(plugin.getDataFolder(), "lang/" + DEFAULT_LANGUAGE + ".yml");
            if (fallback.exists()) {
                return YamlConfiguration.loadConfiguration(fallback);
            }
            // Create an empty configuration as last resort
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(target);
    }

    private void saveLanguageResource(final String resourcePath) {
        final File target = new File(plugin.getDataFolder(), resourcePath);
        if (!target.exists()) {
            try (final InputStream in = plugin.getResource(resourcePath)) {
                if (in != null) {
                    java.nio.file.Files.copy(in, target.toPath());
                }
            } catch (final IOException exception) {
                logger.error("Failed to save language resource: " + resourcePath, exception);
            }
        }
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

    public LanguageConfig language() {
        return languageConfig;
    }
}
