package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.config.CurseCatalog;
import icu.nyat.kusunoki.deenchantment.config.PluginConfig;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEnchantmentEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Base class for all curse behaviour listeners. Provides convenient access to
 * configuration values, enchantment levels, and permission checks.
 */
public abstract class AbstractCurseHandler implements Listener {

    protected final JavaPlugin plugin;
    protected final ConfigService configService;
    protected final EnchantTools enchantTools;
    protected final RegisteredCurse curse;

    private final String permissionNode;

    protected AbstractCurseHandler(final JavaPlugin plugin,
                                   final ConfigService configService,
                                   final EnchantTools enchantTools,
                                   final RegisteredCurse curse) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.enchantTools = Objects.requireNonNull(enchantTools, "enchantTools");
        this.curse = Objects.requireNonNull(curse, "curse");
        this.permissionNode = "deenchantment.enchants." + curse.definition().id().key();
    }

    protected final ConfigurationSection configuration() {
        final CurseCatalog catalog = configService.curses();
        return catalog == null ? null : catalog.get(curse.definition().id().key());
    }

    protected final boolean hasPermission(final LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return true;
        }
        final PluginConfig pluginConfig = configService.plugin();
        if (pluginConfig == null || !pluginConfig.isRequirePermissions()) {
            return true;
        }
        return player.hasPermission(permissionNode);
    }

    protected final int getLevel(final ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        return stack.getEnchantmentLevel(curse);
    }

    protected final int getLevel(final DeEnchantmentEvent event) {
        return event == null ? 0 : event.getCurseLevel(curse);
    }

    protected final int getArmorLevel(final LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        return enchantTools.getLevelCount(entity, curse);
    }

    protected final boolean hasCurse(final ItemStack stack) {
        return getLevel(stack) > 0;
    }

    protected final String configString(final String path, final String def) {
        return configString(def, path);
    }

    protected final String configString(final String def, final String... paths) {
        final ConfigurationSection section = configuration();
        if (section == null || paths == null) {
            return color(def);
        }
        for (String path : paths) {
            if (path == null || path.isEmpty()) {
                continue;
            }
            if (section.contains(path)) {
                return color(section.getString(path, def));
            }
        }
        return color(def);
    }

    protected final boolean configBoolean(final String path, final boolean def) {
        return configBoolean(def, path);
    }

    protected final boolean configBoolean(final boolean def, final String... paths) {
        final ConfigurationSection section = configuration();
        if (section == null || paths == null) {
            return def;
        }
        for (String path : paths) {
            if (path == null || path.isEmpty()) {
                continue;
            }
            if (section.contains(path)) {
                return section.getBoolean(path, def);
            }
        }
        return def;
    }

    protected final double configDouble(final String path, final double def) {
        return configDouble(def, path);
    }

    protected final double configDouble(final double def, final String... paths) {
        final ConfigurationSection section = configuration();
        if (section == null || paths == null) {
            return def;
        }
        for (String path : paths) {
            if (path == null || path.isEmpty()) {
                continue;
            }
            if (section.contains(path)) {
                return section.getDouble(path, def);
            }
        }
        return def;
    }

    protected final int configInt(final String path, final int def) {
        return configInt(def, path);
    }

    protected final int configInt(final int def, final String... paths) {
        final ConfigurationSection section = configuration();
        if (section == null || paths == null) {
            return def;
        }
        for (String path : paths) {
            if (path == null || path.isEmpty()) {
                continue;
            }
            if (section.contains(path)) {
                return section.getInt(path, def);
            }
        }
        return def;
    }

    protected final String color(final String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Hook for subclasses with resources to clean up when unregistered.
     */
    public void disable() {
        // default no-op
    }
}
