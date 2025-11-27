package icu.nyat.kusunoki.deenchantment.nms;

import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Discovers versioned bridge implementations through {@link ServiceLoader} and
 * selects the highest-priority candidate that reports compatibility with the
 * current runtime.
 */
public final class NmsBridgeLoader {

    private final JavaPlugin plugin;
    private final PluginLogger logger;

    public NmsBridgeLoader(final JavaPlugin plugin, final PluginLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public NmsBridge load() {
        final ClassLoader classLoader = plugin.getClass().getClassLoader();
        final ServiceLoader<NmsBridgeFactory> loader = ServiceLoader.load(NmsBridgeFactory.class, classLoader);
        final Iterator<NmsBridgeFactory> iterator = loader.iterator();
        final List<NmsBridgeFactory> candidates = new ArrayList<>();

        while (true) {
            final NmsBridgeFactory factory;
            try {
                if (!iterator.hasNext()) {
                    break;
                }
                factory = iterator.next();
            } catch (final ServiceConfigurationError error) {
                logger.warn("Failed to load an NMS bridge provider", error.getCause() != null ? error.getCause() : error);
                continue;
            }

            try {
                if (factory.isCompatible()) {
                    candidates.add(factory);
                    logger.debug(() -> "NMS bridge accepted: " + factory.describe());
                } else {
                    logger.debug(() -> "NMS bridge rejected: " + factory.describe());
                }
            } catch (final Throwable error) {
                logger.warn("Failed to evaluate NMS bridge " + factory.getClass().getName(), error);
            }
        }

        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingInt(NmsBridgeFactory::priority).reversed());
            final NmsBridgeFactory winner = candidates.get(0);
            logger.info("Using NMS bridge: " + winner.describe());
            return winner.create();
        }

        logger.warn("No compatible NMS bridge found; using fallback Bukkit shim");
        return new FallbackNmsBridge();
    }
}
