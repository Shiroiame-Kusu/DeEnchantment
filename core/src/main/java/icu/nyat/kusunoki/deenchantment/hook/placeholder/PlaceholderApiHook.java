package icu.nyat.kusunoki.deenchantment.hook.placeholder;

import icu.nyat.kusunoki.deenchantment.hook.PluginHook;
import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Integrates with PlaceholderAPI so that all plugin messages can expand
 * arbitrary placeholders defined by other plugins.
 */
public final class PlaceholderApiHook implements PluginHook, PlaceholderText.PlaceholderAdapter {

    private final JavaPlugin plugin;
    private final PluginLogger logger;
    private boolean active;

    public PlaceholderApiHook(final JavaPlugin plugin,
                              final PluginLogger logger) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public String name() {
        return "PlaceholderAPI";
    }

    @Override
    public void enable() {
        PlaceholderText.setAdapter(this);
        active = true;
        logger.info("PlaceholderAPI detected; enabling placeholder expansion for messages");
    }

    @Override
    public void disable() {
        PlaceholderText.reset(this);
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String apply(final CommandSender sender, final String message) {
        if (!active || message == null || message.isEmpty()) {
            return message == null ? "" : message;
        }
        final OfflinePlayer offline = resolvePlayer(sender);
        try {
            return PlaceholderAPI.setPlaceholders(offline, message);
        } catch (final Throwable throwable) {
            logger.debug(() -> "Failed to resolve PlaceholderAPI tokens: " + throwable.getMessage());
            return message;
        }
    }

    private OfflinePlayer resolvePlayer(final CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        if (sender instanceof OfflinePlayer offline) {
            return offline;
        }
        return null;
    }
}
