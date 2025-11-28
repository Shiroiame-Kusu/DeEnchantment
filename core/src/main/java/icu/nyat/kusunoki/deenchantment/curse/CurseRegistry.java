package icu.nyat.kusunoki.deenchantment.curse;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.config.CurseCatalog;
import icu.nyat.kusunoki.deenchantment.config.LanguageConfig;
import icu.nyat.kusunoki.deenchantment.config.PluginConfig;
import icu.nyat.kusunoki.deenchantment.nms.NmsBridge;
import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class CurseRegistry {

    private final ConfigService configService;
    private final PluginLogger logger;
    private final NmsBridge nmsBridge;

    private final Map<CurseId, RegisteredCurse> active = new EnumMap<>(CurseId.class);
    private final Map<CurseId, Permission> permissions = new EnumMap<>(CurseId.class);

    public CurseRegistry(final ConfigService configService,
                         final PluginLogger logger,
                         final NmsBridge nmsBridge) {
        this.configService = configService;
        this.logger = logger;
        this.nmsBridge = nmsBridge;
    }

    public void reload() {
        final PluginConfig config = configService.plugin();
        final CurseCatalog catalog = configService.curses();
        final LanguageConfig lang = configService.language();
        final boolean hardReset = nmsBridge.supportsHardReset();

        if (hardReset) {
            nmsBridge.unregisterAll();
            active.clear();
        }

        clearPermissions();
        nmsBridge.prepareRegistration();

        int registered = 0;
        int total = 0;
        for (final CurseId id : CurseId.values()) {
            final CurseDefinition definition = CurseDefinition.from(id, catalog.get(id.key()), lang);
            if (!definition.enabled()) {
                active.remove(id);
                continue;
            }
            total++;

            RegisteredCurse curse = active.get(id);
            if (curse == null) {
                curse = findExisting(id);
            }

            if (curse == null) {
                curse = new RegisteredCurse(definition);
                if (!nmsBridge.register(curse)) {
                    final RegisteredCurse fallback = findExisting(id);
                    if (fallback == null) {
                        logger.warn("Unable to register curse " + definition.displayName());
                        continue;
                    }
                    curse = fallback;
                }
            }

            curse.refreshDefinition(definition);
            active.put(id, curse);

            final Permission permission = createPermission(id);
            permissions.put(id, permission);
            Bukkit.getPluginManager().addPermission(permission);
            registered++;
            if (!config.isCleanConsole()) {
                logger.info("Registered curse " + toAnsiColor(definition.displayName()));
            }
        }

        nmsBridge.freezeRegistration();
        logger.info("Registered " + registered + "/" + total + " curses");
    }

    /**
     * Converts Minecraft color codes (§ and &) to ANSI escape codes for console output.
     */
    private String toAnsiColor(final String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input
                .replaceAll("(?i)[§&]0", "\u001B[30m")   // Black
                .replaceAll("(?i)[§&]1", "\u001B[34m")   // Dark Blue
                .replaceAll("(?i)[§&]2", "\u001B[32m")   // Dark Green
                .replaceAll("(?i)[§&]3", "\u001B[36m")   // Dark Aqua
                .replaceAll("(?i)[§&]4", "\u001B[31m")   // Dark Red
                .replaceAll("(?i)[§&]5", "\u001B[35m")   // Dark Purple
                .replaceAll("(?i)[§&]6", "\u001B[33m")   // Gold
                .replaceAll("(?i)[§&]7", "\u001B[37m")   // Gray
                .replaceAll("(?i)[§&]8", "\u001B[90m")   // Dark Gray
                .replaceAll("(?i)[§&]9", "\u001B[94m")   // Blue
                .replaceAll("(?i)[§&]a", "\u001B[92m")   // Green
                .replaceAll("(?i)[§&]b", "\u001B[96m")   // Aqua
                .replaceAll("(?i)[§&]c", "\u001B[91m")   // Red
                .replaceAll("(?i)[§&]d", "\u001B[95m")   // Light Purple
                .replaceAll("(?i)[§&]e", "\u001B[93m")   // Yellow
                .replaceAll("(?i)[§&]f", "\u001B[97m")   // White
                .replaceAll("(?i)[§&]l", "\u001B[1m")    // Bold
                .replaceAll("(?i)[§&]o", "\u001B[3m")    // Italic
                .replaceAll("(?i)[§&]n", "\u001B[4m")    // Underline
                .replaceAll("(?i)[§&]m", "\u001B[9m")    // Strikethrough
                .replaceAll("(?i)[§&]k", "")             // Obfuscated (not supported)
                .replaceAll("(?i)[§&]r", "\u001B[0m")    // Reset
                + "\u001B[0m";  // Always reset at end
    }

    public void unregisterAll() {
        active.clear();
        clearPermissions();
        nmsBridge.unregisterAll();
    }

    private Permission createPermission(final CurseId id) {
        final String node = "deenchantment.enchants." + id.key().toLowerCase(Locale.ROOT);
        return new Permission(node, PermissionDefault.TRUE);
    }

    public Optional<RegisteredCurse> find(final CurseId id) {
        return Optional.ofNullable(active.get(id));
    }

    public Optional<RegisteredCurse> find(final String key) {
        return CurseId.fromKey(key).flatMap(this::find);
    }

    public Collection<RegisteredCurse> active() {
        return Collections.unmodifiableCollection(active.values());
    }

    private void clearPermissions() {
        permissions.values().forEach(Bukkit.getPluginManager()::removePermission);
        permissions.clear();
    }

    private RegisteredCurse findExisting(final CurseId id) {
        final Enchantment enchantment = Enchantment.getByKey(id.namespacedKey());
        if (enchantment instanceof RegisteredCurse curse) {
            return curse;
        }
        return null;
    }
}
