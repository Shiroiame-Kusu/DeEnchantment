package icu.nyat.kusunoki.deenchantment.command;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public interface Subcommand {

    String name();

    String description();

    String permission();

    List<String> aliases();

    default boolean requiresPlayer() {
        return false;
    }

    boolean execute(CommandSender sender, String[] args, PluginContext context);

    default List<String> tabComplete(CommandSender sender, String[] args, PluginContext context) {
        return Collections.emptyList();
    }
}
