package icu.nyat.kusunoki.deenchantment.hook.eco;

import icu.nyat.kusunoki.deenchantment.curse.CurseRegistry;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.hook.PluginHook;
import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Objects;

/**
 * Synchronises curse metadata into EcoEnchants so custom lore shows up there as well.
 */
public final class EcoEnchantsHook implements PluginHook {

    private static final String[] CONFIG_CANDIDATES = {
        "vanillaenchants.yml",
        "vanilla-enchants.yml"
    };

    private final JavaPlugin plugin;
    private final PluginLogger logger;
    private final CurseRegistry curseRegistry;

    private YamlConfiguration vanillaFile;
    private File vanillaFileHandle;
    private Plugin ecoPlugin;
    private boolean active;

    public EcoEnchantsHook(final JavaPlugin plugin,
                           final PluginLogger logger,
                           final CurseRegistry curseRegistry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.curseRegistry = Objects.requireNonNull(curseRegistry, "curseRegistry");
    }

    @Override
    public String name() {
        return "EcoEnchants";
    }

    @Override
    public void enable() {
        if (active) {
            syncCurseMetadata();
            return;
        }
        final PluginManager manager = plugin.getServer().getPluginManager();
        final Plugin dependency = manager.getPlugin("EcoEnchants");
        if (dependency == null) {
            logger.debug("EcoEnchants plugin handle was null – skipping hook");
            return;
        }
        final File configFile = resolveConfigFile(dependency);
        if (configFile == null) {
            logger.warn("EcoEnchants lacks vanilla enchant configuration – cannot sync lore");
            return;
        }
        this.ecoPlugin = dependency;
        this.vanillaFileHandle = configFile;
        this.vanillaFile = YamlConfiguration.loadConfiguration(configFile);
        this.active = vanillaFile != null;
        if (!active) {
            logger.warn("Failed to access EcoEnchants vanilla enchant configuration");
            return;
        }
        syncCurseMetadata();
    }

    @Override
    public void disable() {
        active = false;
        vanillaFile = null;
        ecoPlugin = null;
        vanillaFileHandle = null;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private void syncCurseMetadata() {
        if (!active || vanillaFile == null || vanillaFileHandle == null) {
            return;
        }
        try {
            for (RegisteredCurse curse : curseRegistry.active()) {
                final String vanillaKey = curse.definition().id().vanillaKey();
                if (vanillaKey == null || vanillaKey.isEmpty()) {
                    continue;
                }
                final String sectionName = vanillaKey.toLowerCase(Locale.ROOT);
                final ConfigurationSection section = vanillaFile.getConfigurationSection(sectionName) != null
                        ? vanillaFile.getConfigurationSection(sectionName)
                        : vanillaFile.createSection(sectionName);
                if (section == null) {
                    continue;
                }
                section.set("name", colorize(curse.definition().displayName()));
                section.set("description", colorize(curse.definition().description()));
            }
            vanillaFile.save(vanillaFileHandle);
            reloadDisplayCache();
        } catch (final IOException exception) {
            logger.warn("Unable to persist EcoEnchants metadata", exception);
        } catch (final Exception exception) {
            logger.warn("Failed to synchronise EcoEnchants metadata", exception);
        }
    }

    private void reloadDisplayCache() {
        try {
            invokeDisplayCacheReload();
        } catch (final ReflectiveOperationException exception) {
            logger.warn("EcoEnchants display cache reload failed, retrying in 5 seconds...");
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                try {
                    invokeDisplayCacheReload();
                    logger.info("EcoEnchants cache synchronised successfully");
                } catch (final ReflectiveOperationException retry) {
                    logger.warn("EcoEnchants display cache still failing", retry);
                }
            }, 100L);
        }
    }

    private void invokeDisplayCacheReload() throws ReflectiveOperationException {
        final Class<?> cacheClass = Class.forName("com.willfp.ecoenchants.display.DisplayCache");
        try {
            final Method reload = cacheClass.getDeclaredMethod("reload");
            invoke(cacheClass, reload);
            return;
        } catch (final NoSuchMethodException ignored) {
            // fall back to legacy signature below
        }
        final Method legacy = cacheClass.getMethod("onReload");
        invoke(cacheClass, legacy);
    }

    private void invoke(final Class<?> cacheClass, final Method method) throws ReflectiveOperationException {
        if ((method.getModifiers() & Modifier.STATIC) != 0) {
            method.invoke(null);
            return;
        }
        final Field instanceField = cacheClass.getField("INSTANCE");
        final Object instance = instanceField.get(null);
        method.invoke(instance);
    }

    private File resolveConfigFile(final Plugin dependency) {
        for (final String candidate : CONFIG_CANDIDATES) {
            final File file = new File(dependency.getDataFolder(), candidate);
            if (file.exists() || createParent(file)) {
                return file;
            }
        }
        return null;
    }

    private boolean createParent(final File file) {
        final File parent = file.getParentFile();
        if (parent == null) {
            return false;
        }
        return parent.exists() || parent.mkdirs();
    }

    private String colorize(final String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace('§', '&');
    }
}
