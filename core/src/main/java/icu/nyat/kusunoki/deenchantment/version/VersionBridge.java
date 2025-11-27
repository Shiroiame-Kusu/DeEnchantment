package icu.nyat.kusunoki.deenchantment.version;

import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the brittle reflection required to register/unregister custom enchantments
 * across the Paper versions we target. For now we follow the legacy pattern of
 * toggling the private "acceptingNew" flag and editing the static maps directly.
 */
public final class VersionBridge {

    private static final Field ACCEPTING_NEW = lookupOptionalField("acceptingNew");
    private static final Field BY_KEY = lookupOptionalField("byKey");
    private static final Field BY_NAME = lookupOptionalField("byName");

    private final Map<String, RegisteredCurse> registered = new ConcurrentHashMap<>();

    public void prepareRegistration() {
        setAcceptingNew(true);
    }

    public RegisteredCurse register(final RegisteredCurse enchantment) {
        if (tryRegister(enchantment)) {
            remember(enchantment);
            return enchantment;
        }

        final RegisteredCurse existing = locateExisting(enchantment);
        if (existing != null) {
            existing.refreshDefinition(enchantment.definition());
            remember(existing);
            return existing;
        }

        if (supportsHardReset()) {
            unregisterLegacy(enchantment);
            if (tryRegister(enchantment)) {
                remember(enchantment);
                return enchantment;
            }
        }

        return null;
    }

    public void freezeRegistration() {
        setAcceptingNew(false);
    }

    public void unregisterAll() {
        if (!supportsHardReset()) {
            registered.clear();
            return;
        }
        registered.values().forEach(this::unregisterLegacy);
        registered.clear();
    }

    private boolean tryRegister(final Enchantment enchantment) {
        try {
            Enchantment.registerEnchantment(enchantment);
            return true;
        } catch (final IllegalArgumentException alreadyRegistered) {
            return false;
        }
    }

    private RegisteredCurse locateExisting(final RegisteredCurse enchantment) {
        final Enchantment existing = Enchantment.getByKey(enchantment.getKey());
        if (existing instanceof RegisteredCurse curse) {
            return curse;
        }
        return null;
    }

    private void remember(final RegisteredCurse enchantment) {
        registered.put(enchantment.getKey().toString().toLowerCase(Locale.ROOT), enchantment);
    }

    private void unregisterLegacy(final RegisteredCurse enchantment) {
        if (!supportsHardReset()) {
            return;
        }
        final Map<Object, Enchantment> byKey = getMap(BY_KEY);
        final Map<String, Enchantment> byName = getMap(BY_NAME);
        byKey.remove(enchantment.getKey());
        byName.remove(enchantment.getKey().getKey());
    }

    private static Field lookupOptionalField(final String name) {
        try {
            final Field field = Enchantment.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (final NoSuchFieldException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <K> Map<K, Enchantment> getMap(final Field field) {
        try {
            return (Map<K, Enchantment>) field.get(null);
        } catch (final IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read field " + field.getName(), exception);
        }
    }

    private static void setAcceptingNew(final boolean accepting) {
        try {
            if (ACCEPTING_NEW == null) {
                return; // Modern Paper no longer gates registration behind this flag.
            }
            ACCEPTING_NEW.set(null, accepting);
        } catch (final IllegalAccessException exception) {
            throw new IllegalStateException("Unable to toggle acceptingNew", exception);
        }
    }

    public boolean supportsHardReset() {
        return BY_KEY != null && BY_NAME != null;
    }
}
