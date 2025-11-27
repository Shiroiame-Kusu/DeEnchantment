package icu.nyat.kusunoki.deenchantment.hook;

import icu.nyat.kusunoki.deenchantment.curse.CurseRegistry;
import icu.nyat.kusunoki.deenchantment.hook.eco.EcoEnchantsHook;
import icu.nyat.kusunoki.deenchantment.hook.placeholder.PlaceholderApiHook;
import icu.nyat.kusunoki.deenchantment.hook.slimefun.SlimefunHook;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Detects optional plugins at runtime and wires matching hook implementations.
 */
public final class HookManager {

    private final JavaPlugin plugin;
    private final PluginLogger logger;
    private final CurseRegistry curseRegistry;
    private final EnchantTools enchantTools;
    private final List<PluginHook> activeHooks = new ArrayList<>();

    public HookManager(final JavaPlugin plugin,
                       final PluginLogger logger,
                       final CurseRegistry curseRegistry,
                       final EnchantTools enchantTools) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.curseRegistry = Objects.requireNonNull(curseRegistry, "curseRegistry");
        this.enchantTools = Objects.requireNonNull(enchantTools, "enchantTools");
    }

    public void enable() {
        disable();
        final PluginManager manager = plugin.getServer().getPluginManager();
        if (isEnabled(manager, "EcoEnchants")) {
            activeHooks.add(new EcoEnchantsHook(plugin, logger, curseRegistry));
        }
        if (isEnabled(manager, "Slimefun")) {
            activeHooks.add(new SlimefunHook(plugin, logger, enchantTools));
        }
        if (isEnabled(manager, "PlaceholderAPI")) {
            activeHooks.add(new PlaceholderApiHook(plugin, logger));
        }
        for (final PluginHook hook : activeHooks) {
            try {
                hook.enable();
                if (hook.isActive()) {
                    logger.info("Hooked into " + hook.name());
                }
            } catch (final Throwable throwable) {
                logger.warn("Failed to enable hook " + hook.name(), throwable);
            }
        }
    }

    public void reload() {
        enable();
    }

    public void disable() {
        for (final PluginHook hook : activeHooks) {
            try {
                hook.disable();
            } catch (final Throwable throwable) {
                logger.warn("Failed to disable hook " + hook.name(), throwable);
            }
        }
        activeHooks.clear();
    }

    private boolean isEnabled(final PluginManager manager, final String name) {
        final Plugin dependency = manager.getPlugin(name);
        final boolean enabled = dependency != null && dependency.isEnabled();
        if (!enabled) {
            logger.debug(() -> "Optional plugin " + name.toLowerCase(Locale.ROOT) + " not detected; skipping hook");
        }
        return enabled;
    }
}
