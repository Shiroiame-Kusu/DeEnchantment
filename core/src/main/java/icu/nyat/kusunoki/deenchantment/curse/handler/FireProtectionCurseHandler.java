package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityHurtEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Adds a chance to ignite the wearer whenever they take damage.
 */
public final class FireProtectionCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;
    private final int fireTimeRate;

    public FireProtectionCurseHandler(final JavaPlugin plugin,
                                       final ConfigService configService,
                                       final EnchantTools enchantTools,
                                       final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = configDouble(0.05D, "chance-rate", "chanceRate");
        this.fireTimeRate = Math.max(1, configInt(20, "fire-time-rate", "fireTimeRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityHurt(final DeEntityHurtEvent event) {
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity entity = event.getEntity();
        if (!hasPermission(entity)) {
            return;
        }
        final EntityDamageEvent delegate = event.getDelegate();
        final EntityDamageEvent.DamageCause cause = delegate.getCause();
        if (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
            return;
        }
        final double triggerChance = Math.max(0D, level * chanceRate);
        if (ThreadLocalRandom.current().nextDouble() >= triggerChance) {
            return;
        }
        final int fireTicks = Math.max(entity.getFireTicks(), level * fireTimeRate);
        entity.setFireTicks(fireTicks);
    }
}
