package icu.nyat.kusunoki.deenchantment.command;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CommandRegistry {

    private final Map<String, Subcommand> lookup = new LinkedHashMap<>();
    private final Set<Subcommand> ordered = new LinkedHashSet<>();

    public CommandRegistry register(final Subcommand subcommand) {
        Objects.requireNonNull(subcommand, "subcommand");
        ordered.add(subcommand);
        put(subcommand.name(), subcommand);
        for (final String alias : subcommand.aliases()) {
            put(alias, subcommand);
        }
        return this;
    }

    private void put(final String key, final Subcommand subcommand) {
        lookup.put(key.toLowerCase(Locale.ROOT), subcommand);
    }

    public void bind(final JavaPlugin plugin, final PluginContext context) {
        final PluginCommand root = plugin.getCommand("deenchantment");
        if (root == null) {
            throw new IllegalStateException("Command /deenchantment is not registered in plugin.yml");
        }
        final DeEnchantmentCommand executor = new DeEnchantmentCommand(context, lookup, ordered);
        root.setExecutor(executor);
        root.setTabCompleter(executor);
    }

    public Collection<Subcommand> ordered() {
        return Collections.unmodifiableCollection(ordered);
    }
}
