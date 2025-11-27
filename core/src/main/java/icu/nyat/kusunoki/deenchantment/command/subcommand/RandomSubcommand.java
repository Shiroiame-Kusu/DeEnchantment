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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RandomSubcommand extends AbstractSubcommand {

    private static final List<String> TYPE_SUGGESTIONS = List.of("book", "enchant");

    public RandomSubcommand() {
        super(
                "random",
                "Grant a random curse to a player or an item",
                "deenchantment.random",
                List.of("rand"),
                false
        );
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args, final PluginContext context) {
        if (args.length < 2) {
            sendMessage(sender, context, "commands.random-usage", "&cUsage: /deenchantment random <book|enchant> <player> [level]");
            return true;
        }
        final String type = args[0].toLowerCase(Locale.ROOT);
        if (!TYPE_SUGGESTIONS.contains(type)) {
            sendMessage(sender, context, "commands.random-type-invalid", "&cUnsupported type. Use book or enchant.");
            return true;
        }
        final Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sendMessage(sender, context, "commands.random-player-missing", "&cPlayer {0} is not online.", args[1]);
            return true;
        }
        final CurseRegistry registry = context.curses();
        final List<RegisteredCurse> available = new ArrayList<>(registry.active());
        if (available.isEmpty()) {
            sendMessage(sender, context, "commands.random-none", "&cThere are no enabled curses to give.");
            return true;
        }
        final RegisteredCurse curse;
        if ("enchant".equals(type)) {
            final ItemStack hand = target.getInventory().getItemInMainHand();
            if (hand.getType().isAir()) {
                sendMessage(sender, context, "commands.random-hand-empty", "&c{0} is not holding an item.", target.getName());
                return true;
            }
            final List<RegisteredCurse> filtered = available.stream()
                    .filter(c -> c.canEnchantItem(hand))
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) {
                sendMessage(sender, context, "commands.random-none", "&cThere are no applicable curses for that item.");
                return true;
            }
            curse = pickRandom(filtered);
            final int level = resolveLevel(args, curse, context.configs().plugin());
            if (level <= 0) {
                sendMessage(sender, context, "commands.random-level-invalid", "&cLevel must be a positive number.");
                return true;
            }
            applyToItem(hand, curse, level, context);
            sendMessage(sender, context, "commands.random-enchant-success", "&aAdded {0} Lv.{1} to {2}'s held item.",
                    strip(curse.definition().displayName()), level, target.getName());
            return true;
        }
        curse = pickRandom(available);
        final int level = resolveLevel(args, curse, context.configs().plugin());
        if (level <= 0) {
            sendMessage(sender, context, "commands.random-level-invalid", "&cLevel must be a positive number.");
            return true;
        }
        final ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        final EnchantmentStorageMeta meta = (EnchantmentStorageMeta) Objects.requireNonNull(book.getItemMeta(), "meta");
        meta.addStoredEnchant(curse, level, false);
        context.enchantTools().updateLore(meta);
        book.setItemMeta(meta);
        final Map<Integer, ItemStack> overflow = target.getInventory().addItem(book);
        if (!overflow.isEmpty()) {
            overflow.values().forEach(item -> target.getWorld().dropItemNaturally(target.getLocation(), item));
        }
        sendMessage(sender, context, "commands.random-book-success", "&aGave {0} a random curse book: {1} Lv.{2}",
                target.getName(), strip(curse.definition().displayName()), level);
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String[] args, final PluginContext context) {
        if (args.length == 0) {
            return List.of();
        }
        if (args.length == 1) {
            final String token = args[0].toLowerCase(Locale.ROOT);
            return TYPE_SUGGESTIONS.stream()
                    .filter(option -> option.startsWith(token))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            final String token = args[1].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(token))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            final String token = args[2].toLowerCase(Locale.ROOT);
            return IntStream.rangeClosed(1, 5)
                    .mapToObj(Integer::toString)
                    .filter(value -> value.startsWith(token))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private RegisteredCurse pickRandom(final List<RegisteredCurse> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private int resolveLevel(final String[] args, final RegisteredCurse curse, final PluginConfig config) {
        if (args.length >= 3) {
            try {
                final int provided = Integer.parseInt(args[2]);
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
        final int maxLevel = config.isAllowLevelUnlimited()
                ? Math.max(1, curse.getMaxLevel())
                : curse.getMaxLevel();
        if (maxLevel <= 1) {
            return 1;
        }
        return ThreadLocalRandom.current().nextInt(1, maxLevel + 1);
    }

    private void applyToItem(final ItemStack item,
                             final RegisteredCurse curse,
                             final int level,
                             final PluginContext context) {
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

    private String strip(final String value) {
        if (value == null) {
            return "";
        }
        final String stripped = ChatColor.stripColor(value);
        return stripped == null ? value : stripped;
    }
}
