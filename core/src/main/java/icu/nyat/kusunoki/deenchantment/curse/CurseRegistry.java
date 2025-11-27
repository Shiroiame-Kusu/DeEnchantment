package icu.nyat.kusunoki.deenchantment.curse;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.config.CurseCatalog;
import icu.nyat.kusunoki.deenchantment.config.PluginConfig;
import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import icu.nyat.kusunoki.deenchantment.version.VersionBridge;
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
    private final VersionBridge versionBridge;

    private final Map<CurseId, RegisteredCurse> active = new EnumMap<>(CurseId.class);
    private final Map<CurseId, Permission> permissions = new EnumMap<>(CurseId.class);

    public CurseRegistry(final ConfigService configService,
                         final PluginLogger logger,
                         final VersionBridge versionBridge) {
        this.configService = configService;
        this.logger = logger;
        this.versionBridge = versionBridge;
    }

    public void reload() {
        final PluginConfig config = configService.plugin();
        final CurseCatalog catalog = configService.curses();
        final boolean hardReset = versionBridge.supportsHardReset();

        if (hardReset) {
            versionBridge.unregisterAll();
            active.clear();
        }

        clearPermissions();
        versionBridge.prepareRegistration();

        int registered = 0;
        int total = 0;
        for (final CurseId id : CurseId.values()) {
            final CurseDefinition definition = CurseDefinition.from(id, catalog.get(id.key()));
            if (!definition.enabled()) {
                active.remove(id);
                continue;
            }
            total++;

            RegisteredCurse curse = active.get(id);
            if (curse == null) {
                curse = findExisting(id);
            }

            if (curse != null) {
                curse.refreshDefinition(definition);
                active.put(id, curse);
            } else {
                final RegisteredCurse registeredCurse = versionBridge.register(new RegisteredCurse(definition));
                if (registeredCurse == null) {
                    logger.warn("Unable to register curse " + definition.displayName());
                    continue;
                }
                curse = registeredCurse;
                active.put(id, curse);
            }

            final Permission permission = createPermission(id);
            permissions.put(id, permission);
            Bukkit.getPluginManager().addPermission(permission);
            registered++;
            if (!config.isCleanConsole()) {
                logger.info("Registered curse " + definition.displayName());
            }
        }

        versionBridge.freezeRegistration();
        logger.info("Registered " + registered + "/" + total + " curses");
    }

    public void unregisterAll() {
        active.clear();
        clearPermissions();
        versionBridge.unregisterAll();
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
