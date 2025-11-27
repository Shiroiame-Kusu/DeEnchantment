package icu.nyat.kusunoki.deenchantment.curse;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.EnchantmentTarget;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class CurseDefinition {

    private final CurseId id;
    private final boolean enabled;
    private final String displayName;
    private final String description;
    private final EnchantmentTarget target;
    private final double chance;
    private final int maxLevel;
    private final int startLevel;
    private final boolean treasure;
    private final boolean cursed;
    private final Set<String> conflicts;

    private CurseDefinition(final CurseId id,
                            final boolean enabled,
                            final String displayName,
                            final String description,
                            final EnchantmentTarget target,
                            final double chance,
                            final int maxLevel,
                            final int startLevel,
                            final boolean treasure,
                            final boolean cursed,
                            final Set<String> conflicts) {
        this.id = id;
        this.enabled = enabled;
        this.displayName = displayName;
        this.description = description;
        this.target = target;
        this.chance = chance;
        this.maxLevel = maxLevel;
        this.startLevel = startLevel;
        this.treasure = treasure;
        this.cursed = cursed;
        this.conflicts = conflicts;
    }

    public static CurseDefinition from(final CurseId id, final ConfigurationSection section) {
        final boolean enabled = section == null || section.getBoolean("enable", true);
        final String name = section != null ? section.getString("translate-name", id.defaultName()) : id.defaultName();
        final String description = section != null ? section.getString("description", id.defaultDescription()) : id.defaultDescription();
        final String targetKey = section != null ? section.getString("target", id.defaultTarget().name()) : id.defaultTarget().name();
        final EnchantmentTarget target = resolveTarget(targetKey, id);
        final double chance = section != null ? section.getDouble("chance", 0.2D) : 0.2D;
        final int maxLevel = section != null ? section.getInt("max-level", id.defaultMaxLevel()) : id.defaultMaxLevel();
        final int startLevel = section != null ? section.getInt("start-level", id.defaultStartLevel()) : id.defaultStartLevel();
        final boolean treasure = section != null ? section.getBoolean("treasure", id.defaultTreasure()) : id.defaultTreasure();
        final boolean cursed = section != null ? section.getBoolean("cursed", id.defaultCursed()) : id.defaultCursed();
        final Set<String> conflicts = resolveConflicts(section, id);
        return new CurseDefinition(id, enabled, name, description, target, chance, maxLevel, startLevel, treasure, cursed, conflicts);
    }

    private static EnchantmentTarget resolveTarget(final String raw, final CurseId fallback) {
        try {
            return EnchantmentTarget.valueOf(Objects.requireNonNull(raw, "target").toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException | NullPointerException ignored) {
            return fallback.defaultTarget();
        }
    }

    private static Set<String> resolveConflicts(final ConfigurationSection section, final CurseId id) {
        final Set<String> defaults = new HashSet<>(id.defaultConflicts());
        if (section == null) {
            return defaults;
        }
        final List<String> configured = section.getStringList("conflicts");
        if (configured == null || configured.isEmpty()) {
            return defaults;
        }
        for (final String entry : configured) {
            defaults.add(entry.toLowerCase(Locale.ROOT));
        }
        return defaults;
    }

    public CurseId id() {
        return id;
    }

    public boolean enabled() {
        return enabled;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public EnchantmentTarget target() {
        return target;
    }

    public double chance() {
        return chance;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public int startLevel() {
        return startLevel;
    }

    public boolean treasure() {
        return treasure;
    }

    public boolean cursed() {
        return cursed;
    }

    public Set<String> conflicts() {
        return Collections.unmodifiableSet(conflicts);
    }
}
