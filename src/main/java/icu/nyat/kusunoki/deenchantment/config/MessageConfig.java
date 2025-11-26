package icu.nyat.kusunoki.deenchantment.config;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Wrapper for {@code messages.yml} with Adventure-friendly serialization.
 */
public final class MessageConfig {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final FileConfiguration config;
    private final String prefix;

    public MessageConfig(final FileConfiguration config) {
        this.config = Objects.requireNonNull(config, "config");
        this.prefix = colorized("prefix", "&3DeEnchantment &8» ");
    }

    public String prefix() {
        return prefix;
    }

    public String raw(final String path, final String def) {
        return config.getString(path, def);
    }

    public String colorized(final String path, final String def) {
        final String value = config.getString(path);
        return render(value != null ? value : def);
    }

    public String format(final String path, final String def, final Object... args) {
        final String pattern = colorized(path, def);
        return MessageFormat.format(pattern, normalizeArgs(args));
    }

    public String prefixed(final String path, final String def, final Object... args) {
        return prefix + format(path, def, args);
    }

    private Object[] normalizeArgs(final Object[] args) {
        if (args == null || args.length == 0) {
            return new Object[0];
        }
        final Object[] copy = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            copy[i] = args[i] == null ? "" : args[i];
        }
        return copy;
    }

    private String render(final String message) {
        return LEGACY.serialize(LEGACY.deserialize(Objects.requireNonNullElse(message, "")));
    }
}
