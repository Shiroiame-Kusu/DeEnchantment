package icu.nyat.kusunoki.deenchantment.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the parsed contents of {@code DeEnchantments.yml}.
 */
public final class CurseCatalog {

    private final Map<String, ConfigurationSection> entries;

    private CurseCatalog(final Map<String, ConfigurationSection> entries) {
        this.entries = entries;
    }

    public static CurseCatalog from(final FileConfiguration configuration) {
        final Map<String, ConfigurationSection> map = new HashMap<>();
        final ConfigurationSection root = configuration.getConfigurationSection("curses");
        if (root != null) {
            for (final String key : root.getKeys(false)) {
                final ConfigurationSection section = root.getConfigurationSection(key);
                if (section != null) {
                    map.put(key.toLowerCase(), section);
                }
            }
        }
        return new CurseCatalog(map);
    }

    public ConfigurationSection get(final String key) {
        return entries.get(key.toLowerCase());
    }

    public Map<String, ConfigurationSection> entries() {
        return Collections.unmodifiableMap(entries);
    }

    public boolean contains(final String key) {
        return entries.containsKey(key.toLowerCase());
    }
}
