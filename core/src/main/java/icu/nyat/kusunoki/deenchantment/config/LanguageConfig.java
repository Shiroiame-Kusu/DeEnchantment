package icu.nyat.kusunoki.deenchantment.config;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

/**
 * Provides localized curse names and descriptions from language files.
 */
public final class LanguageConfig {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final FileConfiguration config;
    private final String locale;

    public LanguageConfig(final FileConfiguration config, final String locale) {
        this.config = Objects.requireNonNull(config, "config");
        this.locale = Objects.requireNonNull(locale, "locale");
    }

    public String locale() {
        return locale;
    }

    /**
     * Gets the localized display name for a curse.
     *
     * @param curseKey the curse key (e.g., "de_binding_curse")
     * @param def      default value if not found
     * @return the localized name
     */
    public String curseName(final String curseKey, final String def) {
        final ConfigurationSection curses = config.getConfigurationSection("curses");
        if (curses == null) {
            return render(def);
        }
        final ConfigurationSection curse = curses.getConfigurationSection(curseKey);
        if (curse == null) {
            return render(def);
        }
        return render(curse.getString("name", def));
    }

    /**
     * Gets the localized description for a curse.
     *
     * @param curseKey the curse key (e.g., "de_binding_curse")
     * @param def      default value if not found
     * @return the localized description
     */
    public String curseDescription(final String curseKey, final String def) {
        final ConfigurationSection curses = config.getConfigurationSection("curses");
        if (curses == null) {
            return render(def);
        }
        final ConfigurationSection curse = curses.getConfigurationSection(curseKey);
        if (curse == null) {
            return render(def);
        }
        return render(curse.getString("description", def));
    }

    /**
     * Gets a general localized message.
     *
     * @param path the configuration path
     * @param def  default value if not found
     * @return the localized message
     */
    public String message(final String path, final String def) {
        return render(config.getString(path, def));
    }

    private String render(final String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return LEGACY.serialize(LEGACY.deserialize(message));
    }
}
