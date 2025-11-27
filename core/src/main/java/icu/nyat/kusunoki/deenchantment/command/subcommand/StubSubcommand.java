package icu.nyat.kusunoki.deenchantment.command.subcommand;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import icu.nyat.kusunoki.deenchantment.command.AbstractSubcommand;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class StubSubcommand extends AbstractSubcommand {

    public static StubSubcommand create(final String name,
                                        final String description,
                                        final String permission,
                                        final boolean playerOnly,
                                        final List<String> aliases) {
        return new StubSubcommand(name, description, permission, aliases, playerOnly);
    }

    private StubSubcommand(final String name,
                           final String description,
                           final String permission,
                           final List<String> aliases,
                           final boolean playerOnly) {
        super(name, description, permission, aliases, playerOnly);
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args, final PluginContext context) {
        sender.sendMessage(PlaceholderText.apply(sender, context.configs().messages()
                .prefixed("commands.unimplemented", "&cThat feature has not been implemented yet.")));
        return true;
    }
}
