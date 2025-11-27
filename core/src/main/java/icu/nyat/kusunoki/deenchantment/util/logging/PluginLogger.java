package icu.nyat.kusunoki.deenchantment.util.logging;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thin wrapper around Bukkit's plugin logger that provides convenience helpers
 * and guards for verbose debug output.
 */
public final class PluginLogger {

    private final Logger logger;
    private boolean debugEnabled;

    public PluginLogger(final JavaPlugin plugin) {
        this.logger = plugin.getLogger();
    }

    public void setDebugEnabled(final boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public void info(final String message) {
        logger.info(message);
    }

    public void warn(final String message) {
        logger.warning(message);
    }

    public void warn(final String message, final Throwable throwable) {
        logger.log(Level.WARNING, message, throwable);
    }

    public void error(final String message, final Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    public void debug(final String message) {
        if (debugEnabled) {
            logger.fine(message);
        }
    }

    public void debug(final Supplier<String> messageSupplier) {
        if (!debugEnabled || messageSupplier == null) {
            return;
        }
        logger.fine(Objects.requireNonNull(messageSupplier.get(), "message"));
    }
}
