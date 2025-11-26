package icu.nyat.kusunoki.deenchantment.util.text;

import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Lightweight adapter that lets optional placeholder hooks modify outgoing strings
 * without forcing a hard dependency on PlaceholderAPI.
 */
public final class PlaceholderText {

    private static final PlaceholderAdapter IDENTITY = (sender, message) -> message == null ? "" : message;

    private static volatile PlaceholderAdapter adapter = IDENTITY;

    private PlaceholderText() {
        // utility
    }

    public static String apply(final CommandSender sender, final String message) {
        if (message == null || message.isEmpty()) {
            return message == null ? "" : message;
        }
        try {
            return adapter.apply(sender, message);
        } catch (final Exception exception) {
            return message;
        }
    }

    public static void setAdapter(final PlaceholderAdapter newAdapter) {
        adapter = Objects.requireNonNullElse(newAdapter, IDENTITY);
    }

    public static void reset(final PlaceholderAdapter existing) {
        if (adapter == existing) {
            adapter = IDENTITY;
        }
    }

    @FunctionalInterface
    public interface PlaceholderAdapter extends BiFunction<CommandSender, String, String> {
        @Override
        String apply(CommandSender sender, String message);
    }
}
