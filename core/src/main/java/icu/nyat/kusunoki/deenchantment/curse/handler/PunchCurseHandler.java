package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityProjectileEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * Pulls struck targets toward the attacker when cursed projectiles land.
 */
public final class PunchCurseHandler extends AbstractCurseHandler {

    private final double velocityRate;

    public PunchCurseHandler(final JavaPlugin plugin,
                              final ConfigService configService,
                              final EnchantTools enchantTools,
                              final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.velocityRate = Math.max(0D, configDouble(0.8D, "velocity-rate", "velocityRate"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onProjectileDamage(final DeEntityProjectileEvent event) {
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity attacker = event.getEntity();
        if (!hasPermission(attacker)) {
            return;
        }
        final Vector velocity = event.getProjectile().getVelocity();
        if (velocity == null || velocity.lengthSquared() == 0) {
            return;
        }
        final double strength = Math.max(0D, level * velocityRate);
        if (strength <= 0D) {
            return;
        }
        final Entity target = event.getDelegate().getEntity();
        if (target == null) {
            return;
        }
        target.setVelocity(velocity.clone().normalize().multiply(-strength));
    }
}
