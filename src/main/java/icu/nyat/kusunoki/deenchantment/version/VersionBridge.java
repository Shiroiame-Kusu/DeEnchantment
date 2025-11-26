package icu.nyat.kusunoki.deenchantment.version;

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

    private static final Field ACCEPTING_NEW = lookupField("acceptingNew");
    private static final Field BY_KEY = lookupField("byKey");
    private static final Field BY_NAME = lookupField("byName");

    private final Map<String, Enchantment> registered = new ConcurrentHashMap<>();

    public void prepareRegistration() {
        setAcceptingNew(true);
    }

    public boolean register(final Enchantment enchantment) {
        try {
            Enchantment.registerEnchantment(enchantment);
            registered.put(enchantment.getKey().toString().toLowerCase(Locale.ROOT), enchantment);
            return true;
        } catch (final IllegalArgumentException alreadyRegistered) {
            // Happens during reloads; try replacing the previous entry instead of crashing.
            unregister(enchantment);
            Enchantment.registerEnchantment(enchantment);
            registered.put(enchantment.getKey().toString().toLowerCase(Locale.ROOT), enchantment);
            return true;
        }
    }

    public void freezeRegistration() {
        setAcceptingNew(false);
    }

    public void unregisterAll() {
        registered.values().forEach(this::unregister);
        registered.clear();
    }

    private void unregister(final Enchantment enchantment) {
        final Map<Object, Enchantment> byKey = getMap(BY_KEY);
        final Map<String, Enchantment> byName = getMap(BY_NAME);
        byKey.remove(enchantment.getKey());
        byName.remove(enchantment.getKey().getKey());
    }

    private static Field lookupField(final String name) {
        try {
            final Field field = Enchantment.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (final NoSuchFieldException exception) {
            throw new IllegalStateException("Unable to access Enchantment." + name, exception);
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
            ACCEPTING_NEW.set(null, accepting);
        } catch (final IllegalAccessException exception) {
            throw new IllegalStateException("Unable to toggle acceptingNew", exception);
        }
    }
}
