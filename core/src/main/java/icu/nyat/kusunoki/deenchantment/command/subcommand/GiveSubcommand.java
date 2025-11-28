package icu.nyat.kusunoki.deenchantment.command.subcommand;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import icu.nyat.kusunoki.deenchantment.command.AbstractSubcommand;
import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.config.PluginConfig;
import icu.nyat.kusunoki.deenchantment.curse.CurseRegistry;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class GiveSubcommand extends AbstractSubcommand {

    public GiveSubcommand() {
        super(
                "give",
                "Give a player a selected curse book",
                "deenchantment.give",
                List.of(),
                false
        );
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args, final PluginContext context) {
        if (args.length < 2) {
            sendMessage(sender, context, "commands.give-usage", "&cUsage: /deenchantment give <player> <curse> [level]");
            return true;
        }
        final Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sendMessage(sender, context, "commands.give-player-missing", "&cPlayer {0} is not online.", args[0]);
            return true;
        }
        final CurseRegistry registry = context.curses();
        final RegisteredCurse curse = resolveCurse(args[1], registry);
        if (curse == null) {
            sendMessage(sender, context, "commands.give-curse-missing", "&cUnknown or disabled curse: {0}", args[1]);
            return true;
        }
        final Integer parsedLevel = parseLevel(args, curse, context.configs().plugin());
        if (parsedLevel == null) {
            sendMessage(sender, context, "commands.give-level-invalid", "&cLevel must be a positive number.");
            return true;
        }
        final ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        final EnchantmentStorageMeta meta = (EnchantmentStorageMeta) Objects.requireNonNull(book.getItemMeta(), "meta");
        // Use PDC storage for curses to avoid Paper 1.20.6+ Handleable issues
        context.enchantTools().addCurseToPdc(meta, curse, parsedLevel);
        context.enchantTools().updateLore(meta);
        book.setItemMeta(meta);
        final Map<Integer, ItemStack> overflow = target.getInventory().addItem(book);
        if (!overflow.isEmpty()) {
            overflow.values().forEach(item -> target.getWorld().dropItemNaturally(target.getLocation(), item));
        }
        sendMessage(sender, context, "commands.give-success", "&aGave {0} a {1} &7(Level {2})",
                target.getName(), stripFormatting(curse.definition().displayName()), parsedLevel);
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String[] args, final PluginContext context) {
        if (args.length == 0) {
            return List.of();
        }
        if (args.length == 1) {
            final String token = args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(token))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            final String token = args[1].toLowerCase(Locale.ROOT);
            return context.curses().active().stream()
                    .map(curse -> List.of(
                            curse.definition().id().key(),
                            stripFormatting(curse.definition().displayName())
                    ))
                    .flatMap(Collection::stream)
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .filter(value -> value.startsWith(token))
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            final RegisteredCurse curse = resolveCurse(args[1], context.curses());
            final int maxLevels = curse != null ? Math.max(1, curse.getMaxLevel()) : 5;
            final String token = args[2].toLowerCase(Locale.ROOT);
            return IntStream.rangeClosed(1, maxLevels)
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
        final String stripped = stripFormatting(token);
        for (final RegisteredCurse curse : registry.active()) {
            if (stripFormatting(curse.definition().displayName()).equalsIgnoreCase(stripped)) {
                return curse;
            }
        }
        return null;
    }

    private Integer parseLevel(final String[] args, final RegisteredCurse curse, final PluginConfig config) {
        final int provided;
        if (args.length < 3) {
            provided = 1;
        } else {
            try {
                provided = Integer.parseInt(args[2]);
            } catch (final NumberFormatException ignored) {
                return null;
            }
        }
        if (provided <= 0) {
            return null;
        }
        if (config.isAllowLevelUnlimited()) {
            return provided;
        }
        return Math.min(provided, curse.getMaxLevel());
    }

    private void sendMessage(final CommandSender sender,
                             final PluginContext context,
                             final String path,
                             final String def,
                             final Object... args) {
        final MessageConfig messages = context.configs().messages();
            sender.sendMessage(PlaceholderText.apply(sender, messages.prefixed(path, def, args)));
    }

    private String stripFormatting(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        final String plain = ChatColor.stripColor(input);
        return plain == null ? input : plain;
    }
}
