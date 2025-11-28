package icu.nyat.kusunoki.deenchantment.curse;

import icu.nyat.kusunoki.deenchantment.config.LanguageConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.EnchantmentTarget;

import java.util.Collections;
import java.util.HashSet;
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

    /**
     * Creates a CurseDefinition from configuration section and language config.
     * Names and descriptions are loaded from the language file.
     */
    public static CurseDefinition from(final CurseId id, final ConfigurationSection section, final LanguageConfig lang) {
        final boolean enabled = section == null || section.getBoolean("enable", true);
        
        // Get name and description from language file, fall back to CurseId defaults
        final String name = lang != null 
                ? lang.curseName(id.key(), id.defaultName()) 
                : id.defaultName();
        final String description = lang != null 
                ? lang.curseDescription(id.key(), id.defaultDescription()) 
                : id.defaultDescription();
        
        // Use hardcoded defaults from CurseId for enchantment properties
        final EnchantmentTarget target = id.defaultTarget();
        final double chance = 0.2D; // Fixed chance for all curses
        final int maxLevel = id.defaultMaxLevel();
        final int startLevel = id.defaultStartLevel();
        final boolean treasure = id.defaultTreasure();
        final boolean cursed = id.defaultCursed();
        final Set<String> conflicts = new HashSet<>(id.defaultConflicts());
        
        return new CurseDefinition(id, enabled, name, description, target, chance, maxLevel, startLevel, treasure, cursed, conflicts);
    }

    /**
     * @deprecated Use {@link #from(CurseId, ConfigurationSection, LanguageConfig)} instead.
     */
    @Deprecated
    public static CurseDefinition from(final CurseId id, final ConfigurationSection section) {
        return from(id, section, null);
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
