package icu.nyat.kusunoki.deenchantment.nms.v1_20_5;

import icu.nyat.kusunoki.deenchantment.nms.NmsBridge;
import icu.nyat.kusunoki.deenchantment.nms.NmsBridgeFactory;
import org.bukkit.Bukkit;

/**
 * Provides the streamlined bridge for the 1.20.5-1.20.6 server line.
 */
public final class ModernNmsBridgeFactory implements NmsBridgeFactory {

    private static final int PRIORITY = 12005;

    @Override
    public boolean isCompatible() {
        final String minecraftVersion = Bukkit.getMinecraftVersion();
        if (minecraftVersion == null) {
            return false;
        }
        // Match 1.20.5 and 1.20.6 specifically
        return minecraftVersion.startsWith("1.20.5") || minecraftVersion.startsWith("1.20.6");
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public String describe() {
        return "Modern Bukkit enchantment bridge (1.20.5-1.20.6)";
    }

    @Override
    public NmsBridge create() {
        return new ModernNmsBridge();
    }
}
