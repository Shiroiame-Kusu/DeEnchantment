package icu.nyat.kusunoki.deenchantment.command;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

final class DeEnchantmentCommand implements CommandExecutor, TabCompleter {

    private final PluginContext context;
    private final Map<String, Subcommand> lookup;
    private final List<Subcommand> ordered;

    DeEnchantmentCommand(final PluginContext context,
                         final Map<String, Subcommand> lookup,
                         final Iterable<Subcommand> ordered) {
        this.context = Objects.requireNonNull(context, "context");
        this.lookup = Objects.requireNonNull(lookup, "lookup");
        final List<Subcommand> list = new ArrayList<>();
        ordered.forEach(list::add);
        this.ordered = Collections.unmodifiableList(list);
    }

    @Override
    public boolean onCommand(final CommandSender sender,
                             final Command command,
                             final String label,
                             final String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        final Subcommand subcommand = lookup.get(args[0].toLowerCase(Locale.ROOT));
        if (subcommand == null) {
            sendMessage(sender, "commands.unknown", "&cUnknown sub-command: {0}", args[0]);
            return true;
        }
        if (subcommand.permission() != null && !sender.hasPermission(subcommand.permission())) {
            sendMessage(sender, "commands.no-permission", "&cYou do not have permission to use this sub-command.");
            return true;
        }
        if (subcommand.requiresPlayer() && !(sender instanceof Player)) {
            sendMessage(sender, "commands.players-only", "&cOnly players may use this sub-command.");
            return true;
        }
        final String[] remaining = trimArgs(args);
        return subcommand.execute(sender, remaining, context);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender,
                                      final Command command,
                                      final String alias,
                                      final String[] args) {
        if (args.length == 0) {
            return List.of();
        }
        if (args.length == 1) {
            final String input = args[0].toLowerCase(Locale.ROOT);
            return ordered.stream()
                    .filter(sub -> sub.permission() == null || sender.hasPermission(sub.permission()))
                    .map(Subcommand::name)
                    .filter(name -> name.startsWith(input))
                    .collect(Collectors.toList());
        }
        final Subcommand sub = lookup.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            return List.of();
        }
        return sub.tabComplete(sender, trimArgs(args), context);
    }

    private String[] trimArgs(final String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        final String[] trimmed = new String[args.length - 1];
        System.arraycopy(args, 1, trimmed, 0, args.length - 1);
        return trimmed;
    }

    private void showHelp(final CommandSender sender) {
        sendMessage(sender, "commands.help-header", "&bAvailable commands:");
        for (final Subcommand subcommand : ordered) {
            if (subcommand.permission() != null && !sender.hasPermission(subcommand.permission())) {
                continue;
            }
            sendMessage(sender, "commands.help-entry", "&7/deenchantment {0} &f- {1}",
                    subcommand.name(), subcommand.description());
        }
    }

    private void sendMessage(final CommandSender sender, final String path, final String def, final Object... args) {
        final MessageConfig messages = context.configs().messages();
        sender.sendMessage(PlaceholderText.apply(sender, messages.prefixed(path, def, args)));
    }
}
