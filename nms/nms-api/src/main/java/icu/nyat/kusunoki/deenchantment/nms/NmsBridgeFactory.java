package icu.nyat.kusunoki.deenchantment.nms;

/**
 * Service provider interface discovered via {@link java.util.ServiceLoader}.
 * Version-specific modules expose one implementation that decides at runtime
 * whether it can operate on the current server and, if so, instantiates the
 * appropriate {@link NmsBridge}.
 */
public interface NmsBridgeFactory {

    /**
     * @return {@code true} when this factory supports the current runtime
     * environment (Minecraft/Paper version, mappings, etc.). Unsupported
     * factories are ignored silently.
     */
    boolean isCompatible();

    /**
     * @return Priority hint used when multiple factories report compatibility.
     * Higher numbers win so more specific bridges can override generic ones.
     */
    int priority();

    /**
     * @return Short descriptor used for logging/debugging so operators know
     * which bridge powers the current server.
     */
    String describe();

    /**
     * @return A new {@link NmsBridge} instance owned by the caller.
     */
    NmsBridge create();
}
