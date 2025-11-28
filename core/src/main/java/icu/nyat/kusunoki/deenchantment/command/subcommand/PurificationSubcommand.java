package icu.nyat.kusunoki.deenchantment.command.subcommand;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import icu.nyat.kusunoki.deenchantment.command.AbstractSubcommand;
import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.curse.CurseRegistry;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class PurificationSubcommand extends AbstractSubcommand {

    public PurificationSubcommand() {
        super(
                "purification",
                "Convert curses on a player's held item back to vanilla enchantments",
                "deenchantment.purification",
                List.of("pur"),
                false
        );
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args, final PluginContext context) {
        if (args.length < 1) {
            sendMessage(sender, context, "commands.purification-usage", "&cUsage: /deenchantment purification <player>");
            return true;
        }
        final Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sendMessage(sender, context, "commands.purification-player-missing", "&cPlayer {0} is not online.", args[0]);
            return true;
        }
        final ItemStack item = target.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            sendMessage(sender, context, "commands.purification-hand-empty", "&c{0} is not holding an item.", target.getName());
            return true;
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            sendMessage(sender, context, "commands.purification-no-meta", "&cThat item cannot hold enchantments.");
            return true;
        }
        final boolean changed = cleanse(meta, context);
        if (!changed) {
            sendMessage(sender, context, "commands.purification-none", "&eNo curses were found on that item.");
            return true;
        }
        context.enchantTools().updateLore(meta);
        item.setItemMeta(meta);
        sendMessage(sender, context, "commands.purification-success", "&aConverted curses on {0}'s held item.", target.getName());
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String[] args, final PluginContext context) {
        if (args.length == 1) {
            final String token = args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(token))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private boolean cleanse(final ItemMeta meta, final PluginContext context) {
        boolean modified = false;
        // First check enchantments map (for backwards compatibility with old items)
        if (meta instanceof EnchantmentStorageMeta storage) {
            final Map<Enchantment, Integer> stored = Map.copyOf(storage.getStoredEnchants());
            for (Map.Entry<Enchantment, Integer> entry : stored.entrySet()) {
                final int level = entry.getValue();
                modified |= cleanseSingle(entry.getKey(), storage::removeStoredEnchant, base -> storage.addStoredEnchant(base, level, true));
            }
        } else {
            final Map<Enchantment, Integer> enchants = Map.copyOf(meta.getEnchants());
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                final int level = entry.getValue();
                modified |= cleanseSingle(entry.getKey(), meta::removeEnchant, base -> meta.addEnchant(base, level, true));
            }
        }
        // Also check PDC for curses stored there (Paper 1.20.6+ approach)
        final EnchantTools enchantTools = context.enchantTools();
        final CurseRegistry curseRegistry = context.curses();
        final Map<String, Integer> pdcCurses = enchantTools.getCursesFromPdc(meta);
        if (!pdcCurses.isEmpty()) {
            for (Map.Entry<String, Integer> entry : pdcCurses.entrySet()) {
                final String curseKey = entry.getKey();
                final int level = entry.getValue();
                final Optional<RegisteredCurse> curseOpt = curseRegistry.find(curseKey);
                if (curseOpt.isPresent()) {
                    final RegisteredCurse curse = curseOpt.get();
                    final Optional<Enchantment> vanilla = curse.vanillaEnchantment();
                    if (vanilla.isPresent()) {
                        if (meta instanceof EnchantmentStorageMeta storage) {
                            storage.addStoredEnchant(vanilla.get(), level, true);
                        } else {
                            meta.addEnchant(vanilla.get(), level, true);
                        }
                        modified = true;
                    }
                }
            }
            // Clear the PDC curses after conversion
            enchantTools.copyCursesToPdc(meta, Map.of());
        }
        return modified;
    }

    private boolean cleanseSingle(final Enchantment enchantment,
                                   final Predicate<Enchantment> remover,
                                   final Consumer<Enchantment> adder) {
        if (!(enchantment instanceof RegisteredCurse curse)) {
            return false;
        }
        remover.test(enchantment);
        final Optional<Enchantment> vanilla = curse.vanillaEnchantment();
        vanilla.ifPresent(adder);
        return true;
    }

    private void sendMessage(final CommandSender sender,
                             final PluginContext context,
                             final String path,
                             final String def,
                             final Object... args) {
        final MessageConfig messages = context.configs().messages();
                            sender.sendMessage(PlaceholderText.apply(sender, messages.prefixed(path, def, args)));
    }
}
