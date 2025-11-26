package icu.nyat.kusunoki.deenchantment.command.subcommand;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import icu.nyat.kusunoki.deenchantment.command.AbstractSubcommand;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class ReloadSubcommand extends AbstractSubcommand {

    public ReloadSubcommand() {
        super("reload",
                "Reload all DeEnchantment configuration files",
                "deenchantment.reload",
                List.of("rl"),
                false);
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args, final PluginContext context) {
        try {
            context.reloadConfigs();
            sender.sendMessage(PlaceholderText.apply(sender, context.configs().messages()
                    .prefixed("commands.reload-success", "&aReloaded configuration files.")));
        } catch (final Exception exception) {
            context.logger().error("Failed to reload configuration", exception);
            sender.sendMessage(PlaceholderText.apply(sender, context.configs().messages()
                    .prefixed("commands.reload-failure", "&cReload failed: {0}", exception.getMessage())));
        }
        return true;
    }
}
