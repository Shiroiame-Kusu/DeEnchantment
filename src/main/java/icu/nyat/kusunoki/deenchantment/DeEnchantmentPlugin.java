package icu.nyat.kusunoki.deenchantment;

import icu.nyat.kusunoki.deenchantment.bootstrap.PluginContext;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Primary Paper/Spigot entry point for the rewritten DeEnchantment plugin.
 */
public final class DeEnchantmentPlugin extends JavaPlugin {

    private static DeEnchantmentPlugin instance;
    private PluginContext context;

    public static DeEnchantmentPlugin getInstance() {
        return instance;
    }

    public PluginContext getContext() {
        return context;
    }

    @Override
    public void onLoad() {
        instance = this;
        context = new PluginContext(this);
        context.load();
    }

    @Override
    public void onEnable() {
        context.enable();
    }

    @Override
    public void onDisable() {
        if (context != null) {
            context.disable();
        }
    }
}
