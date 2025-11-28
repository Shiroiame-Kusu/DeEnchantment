package icu.nyat.kusunoki.deenchantment.nms;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Abstraction layer over Bukkit/Paper's enchantment registration internals.
 * Implementations live in versioned NMS modules and hide the reflection or
 * registry plumbing required by a specific server release line.
 */
public interface NmsBridge {

    /**
     * Prepare the underlying registry so that new enchantments can be added.
     * Legacy server versions require toggling internal flags before custom
     * enchantments may be inserted.
     */
    void prepareRegistration();

    /**
     * Attempt to register the provided enchantment instance with the server.
     *
     * @param enchantment The enchantment to register.
     * @return {@code true} when the enchantment is now registered, {@code false}
     * when the implementation was unable to install it (usually because an
     * entry already exists and cannot be replaced safely).
     */
    boolean register(Enchantment enchantment);

    /**
     * Restore the registry's immutable state once all registrations are done.
     */
    void freezeRegistration();

    /**
     * Remove every enchantment previously registered through this bridge.
     * Implementations that cannot undo the registration should simply forget
     * their internal bookkeeping so the caller can continue safely.
     */
    void unregisterAll();

    /**
     * @return {@code true} when the implementation can fully remove enchantments
     * from the backing registry, {@code false} when only soft reconfiguration is
     * possible.
     */
    boolean supportsHardReset();

    /**
     * Add enchantments to an item using NMS to bypass Bukkit's Handleable check.
     * This is needed for Paper 1.20.6+ where custom enchantments must implement
     * Handleable to be added via the Bukkit API.
     *
     * @param item The item to enchant
     * @param enchantments Map of enchantments and their levels to add
     * @param asStoredEnchants If true, add as stored enchants (for enchanted books)
     * @return true if successful
     */
    default boolean addEnchantsNms(ItemStack item, Map<Enchantment, Integer> enchantments, boolean asStoredEnchants) {
        // Default implementation uses Bukkit API - override in version-specific bridges
        if (item == null || enchantments == null || enchantments.isEmpty()) {
            return false;
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        try {
            if (asStoredEnchants && meta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta storage) {
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    storage.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                }
            } else {
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    meta.addEnchant(entry.getKey(), entry.getValue(), true);
                }
            }
            item.setItemMeta(meta);
            return true;
        } catch (final Throwable e) {
            return false;
        }
    }
}
