package icu.nyat.kusunoki.deenchantment.metrics;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Wraps bStats metrics startup/shutdown to keep PluginContext cleaner.
 */
public final class MetricsService {

    private final JavaPlugin plugin;
    private Metrics metrics;

    public MetricsService(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (metrics == null) {
            metrics = new Metrics(plugin, 13440);
        }
    }

    public void stop() {
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
    }
}
