package icu.nyat.kusunoki.deenchantment.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Wrapper for language configuration messages with color code support.
 * Messages are loaded from the active language file (e.g., lang/zh_CN.yml).
 */
public final class MessageConfig {

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
        if (message == null || message.isEmpty()) {
            return "";
        }
        // Convert & color codes to § for Minecraft
        return message.replace('&', '§');
    }
}
