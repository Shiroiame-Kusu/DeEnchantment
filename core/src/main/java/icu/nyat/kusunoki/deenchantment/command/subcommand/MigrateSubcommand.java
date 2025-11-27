package icu.nyat.kusunoki.deenchantment.command.subcommand;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import icu.nyat.kusunoki.deenchantment.command.AbstractSubcommand;
import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.List;

public final class MigrateSubcommand extends AbstractSubcommand {

    public MigrateSubcommand() {
        super(
                "migrate",
                "Migrate legacy v1 configuration files",
                "deenchantment.migrate",
                List.of("convert"),
                false
        );
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args, final PluginContext context) {
        final ConfigService configs = context.configs();
        final File legacy = new File(context.plugin().getDataFolder(), "old_config.yml");
        if (!configs.migrateLegacyConfig(legacy)) {
            sendMessage(sender, context, "commands.migrate-missing", "&cPlace old_config.yml in the plugin folder first.");
            return true;
        }
        sendMessage(sender, context, "commands.migrate-success", "&aLegacy configuration migrated successfully.");
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
