package icu.nyat.kusunoki.deenchantment.nms;

import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Last-resort bridge that relies solely on Bukkit's public API. It cannot
 * perform hard resets but still allows the plugin to operate on unknown
 * versions instead of failing to start.
 */
final class FallbackNmsBridge implements NmsBridge {

    private static final Method REGISTER_METHOD;

    static {
        Method method = null;
        try {
            method = Enchantment.class.getDeclaredMethod("registerEnchantment", Enchantment.class);
            method.setAccessible(true);
        } catch (final NoSuchMethodException ignored) {
            // Public registration API was removed (Paper 1.20.4+), so fallback support becomes unavailable.
        }
        REGISTER_METHOD = method;
    }

    private final Map<String, Enchantment> registered = new ConcurrentHashMap<>();

    @Override
    public void prepareRegistration() {
        // Nothing to toggle through the public API.
    }

    @Override
    public boolean register(final Enchantment enchantment) {
        if (enchantment == null || REGISTER_METHOD == null) {
            return false;
        }
        try {
            REGISTER_METHOD.invoke(null, enchantment);
            registered.put(enchantment.getKey().toString().toLowerCase(Locale.ROOT), enchantment);
            return true;
        } catch (final InvocationTargetException target) {
            if (target.getCause() instanceof IllegalArgumentException) {
                return false;
            }
            throw new RuntimeException("Failed to register enchantment", target.getCause());
        } catch (final IllegalAccessException reflectionError) {
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
