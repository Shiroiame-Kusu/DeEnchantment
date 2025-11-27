package icu.nyat.kusunoki.deenchantment.command.subcommand;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import icu.nyat.kusunoki.deenchantment.command.AbstractSubcommand;
import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.config.PluginConfig;
import icu.nyat.kusunoki.deenchantment.curse.CurseRegistry;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class AddSubcommand extends AbstractSubcommand {

    public AddSubcommand() {
        super(
                "add",
                "Add a specific curse to the held item",
                "deenchantment.add",
                List.of(),
                true
        );
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args, final PluginContext context) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, context, "commands.players-only", "&cOnly players may use this sub-command.");
            return true;
        }
        if (args.length < 1) {
            sendMessage(sender, context, "commands.add-usage", "&cUsage: /deenchantment add <curse> [level]");
            return true;
        }
        final CurseRegistry registry = context.curses();
        final RegisteredCurse curse = resolveCurse(args[0], registry);
        if (curse == null) {
            sendMessage(sender, context, "commands.add-curse-missing", "&cUnknown or disabled curse: {0}", args[0]);
            return true;
        }
        final int level = parseLevel(args, curse, context.configs().plugin());
        if (level <= 0) {
            sendMessage(sender, context, "commands.add-level-invalid", "&cLevel must be a positive number.");
            return true;
        }
        final ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            sendMessage(sender, context, "commands.add-hand-empty", "&cYou must hold an item to add curses.");
            return true;
        }
        applyCurse(held, curse, level, context);
        sendMessage(sender, context, "commands.add-success", "&aAdded {0} Level {1} to your held item.",
                strip(curse.definition().displayName()), level);
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String[] args, final PluginContext context) {
        if (args.length == 0) {
            return List.of();
        }
        if (args.length == 1) {
            final String token = args[0].toLowerCase(Locale.ROOT);
            return context.curses().active().stream()
                    .map(curse -> List.of(curse.definition().id().key(), strip(curse.definition().displayName())))
                    .flatMap(List::stream)
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .filter(value -> value.startsWith(token))
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            final RegisteredCurse curse = resolveCurse(args[0], context.curses());
            final int max = curse != null ? Math.max(1, curse.getMaxLevel()) : 5;
            final String token = args[1].toLowerCase(Locale.ROOT);
            return IntStream.rangeClosed(1, max)
                    .mapToObj(Integer::toString)
                    .filter(value -> value.startsWith(token))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private RegisteredCurse resolveCurse(final String token, final CurseRegistry registry) {
        final String normalized = token.toLowerCase(Locale.ROOT);
        final Optional<RegisteredCurse> direct = registry.find(normalized.startsWith("de_") ? normalized : "de_" + normalized);
        if (direct.isPresent()) {
            return direct.get();
        }
        final String stripped = strip(token);
        for (final RegisteredCurse curse : registry.active()) {
            if (strip(curse.definition().displayName()).equalsIgnoreCase(stripped)) {
                return curse;
            }
        }
        return null;
    }

    private int parseLevel(final String[] args, final RegisteredCurse curse, final PluginConfig config) {
        if (args.length >= 2) {
            try {
                final int provided = Integer.parseInt(args[1]);
                if (provided <= 0) {
                    return -1;
                }
                if (config.isAllowLevelUnlimited()) {
                    return provided;
                }
                return Math.min(provided, curse.getMaxLevel());
            } catch (final NumberFormatException ignored) {
                return -1;
            }
        }
        return 1;
    }

    private void applyCurse(final ItemStack item, final RegisteredCurse curse, final int level, final PluginContext context) {
        final var meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        if (meta instanceof EnchantmentStorageMeta storage) {
            storage.addStoredEnchant(curse, level, true);
        } else {
            meta.addEnchant(curse, level, true);
        }
        context.enchantTools().updateLore(meta);
        item.setItemMeta(meta);
    }

    private void sendMessage(final CommandSender sender,
                             final PluginContext context,
                             final String path,
                             final String def,
                             final Object... args) {
        final MessageConfig messages = context.configs().messages();
        sender.sendMessage(PlaceholderText.apply(sender, messages.prefixed(path, def, args)));
    }

    private String strip(final String input) {
        if (input == null) {
            return "";
        }
        final String stripped = ChatColor.stripColor(input);
        return stripped == null ? input : stripped;
    }
}
