package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityShootBowEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Sets the shooter on fire when they shoot a flame-cursed bow.
 */
public final class FlameCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;
    private final int fireTickRate;

    public FlameCurseHandler(final JavaPlugin plugin,
                              final ConfigService configService,
                              final EnchantTools enchantTools,
                              final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, configDouble(0.3D, "chance-rate", "chanceRate"));
        this.fireTickRate = Math.max(1, configInt(40, "fire-tick-rate", "fireTickRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onShootBow(final DeEntityShootBowEvent event) {
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity shooter = event.getEntity();
        if (!hasPermission(shooter)) {
            return;
        }
        final double chance = Math.min(1.0D, level * chanceRate);
        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }
        // Set shooter on fire
        final int fireTicks = level * fireTickRate;
        shooter.setFireTicks(Math.max(shooter.getFireTicks(), fireTicks));
    }
}
