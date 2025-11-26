package icu.nyat.kusunoki.deenchantment.command.subcommand;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import icu.nyat.kusunoki.deenchantment.command.AbstractSubcommand;
import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class UpdateSubcommand extends AbstractSubcommand {

    public UpdateSubcommand() {
        super(
                "update",
                "Refresh curse lore lines on a player's held item",
                "deenchantment.update",
                List.of("refresh"),
                false
        );
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args, final PluginContext context) {
        final Player target;
        if (args.length >= 1) {
            final Player specified = Bukkit.getPlayerExact(args[0]);
            if (specified == null) {
                sendMessage(sender, context, "commands.update-player-missing", "&cPlayer {0} is not online.", args[0]);
                return true;
            }
            target = specified;
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sendMessage(sender, context, "commands.players-only", "&cOnly players may use this sub-command.");
            return true;
        }
        if (target.getInventory().getItemInMainHand().getType().isAir()) {
            sendMessage(sender, context, "commands.update-hand-empty", "&c{0} is not holding an item.", target.getName());
            return true;
        }
        final var item = target.getInventory().getItemInMainHand();
        final var meta = item.getItemMeta();
        if (meta == null) {
            sendMessage(sender, context, "commands.update-no-meta", "&cThat item cannot hold lore.");
            return true;
        }
        context.enchantTools().updateLore(meta);
        item.setItemMeta(meta);
        sendMessage(sender, context, "commands.update-success", "&aUpdated curse lore for {0}.", target.getName());
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

    private void sendMessage(final CommandSender sender,
                             final PluginContext context,
                             final String path,
                             final String def,
                             final Object... args) {
        final MessageConfig messages = context.configs().messages();
                            sender.sendMessage(PlaceholderText.apply(sender, messages.prefixed(path, def, args)));
    }
}