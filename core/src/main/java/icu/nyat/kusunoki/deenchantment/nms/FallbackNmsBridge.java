package icu.nyat.kusunoki.deenchantment.nms;

import org.bukkit.enchantments.Enchantment;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Last-resort bridge that relies solely on Bukkit's public API. It cannot
 * perform hard resets but still allows the plugin to operate on unknown
 * versions instead of failing to start.
 */
final class FallbackNmsBridge implements NmsBridge {

    private final Map<String, Enchantment> registered = new ConcurrentHashMap<>();

    @Override
    public void prepareRegistration() {
        // Nothing to toggle through the public API.
    }

    @Override
    public boolean register(final Enchantment enchantment) {
        if (enchantment == null) {
            return false;
        }
        try {
            Enchantment.registerEnchantment(enchantment);
            registered.put(enchantment.getKey().toString().toLowerCase(Locale.ROOT), enchantment);
            return true;
        } catch (final IllegalArgumentException alreadyRegistered) {
            return false;
        }
    }

    @Override
    public void freezeRegistration() {
        // Registries remain mutable via the public API.
    }

    @Override
    public void unregisterAll() {
        registered.clear();
    }

    @Override
    public boolean supportsHardReset() {
        return false;
    }
}
