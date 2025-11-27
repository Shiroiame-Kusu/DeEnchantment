package icu.nyat.kusunoki.deenchantment.nms;

import org.bukkit.enchantments.Enchantment;

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
}
