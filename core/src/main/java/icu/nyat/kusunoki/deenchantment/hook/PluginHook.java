package icu.nyat.kusunoki.deenchantment.hook;

/**
 * Simple lifecycle contract for optional plugin integrations.
 */
public interface PluginHook {

    /**
     * Human friendly name used in logs.
     */
    String name();

    /**
     * Attempt to enable the hook. Implementations should set {@link #isActive()} accordingly.
     */
    void enable();

    /**
     * Tear down any registered listeners or background tasks.
     */
    void disable();

    /**
     * @return {@code true} if the hook successfully attached to the external plugin.
     */
    boolean isActive();
}
