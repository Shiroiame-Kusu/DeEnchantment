package icu.nyat.kusunoki.deenchantment.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractSubcommand implements Subcommand {

    private final String name;
    private final String description;
    private final String permission;
    private final List<String> aliases;
    private final boolean playerOnly;

    protected AbstractSubcommand(final String name,
                                 final String description,
                                 final String permission,
                                 final List<String> aliases,
                                 final boolean playerOnly) {
        this.name = Objects.requireNonNull(name, "name").toLowerCase();
        this.description = Objects.requireNonNull(description, "description");
        this.permission = permission;
        this.playerOnly = playerOnly;
        if (aliases == null || aliases.isEmpty()) {
            this.aliases = Collections.emptyList();
        } else {
            final List<String> list = new ArrayList<>(aliases.size());
            for (final String alias : aliases) {
                list.add(alias.toLowerCase());
            }
            this.aliases = Collections.unmodifiableList(list);
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String permission() {
        return permission;
    }

    @Override
    public List<String> aliases() {
        return aliases;
    }

    @Override
    public boolean requiresPlayer() {
        return playerOnly;
    }
}
