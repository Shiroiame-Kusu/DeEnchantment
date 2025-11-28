package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityHurtEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * When the wearer takes damage from an entity, deals additional damage to the wearer instead of reflecting it.
 * (Reverse thorns - hurts self more when hit)
 */
public final class ThornsCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;
    private final double damageRate;

    public ThornsCurseHandler(final JavaPlugin plugin,
                              final ConfigService configService,
                              final EnchantTools enchantTools,
                              final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, configDouble(0.15D, "chance-rate", "chanceRate"));
        this.damageRate = Math.max(0D, configDouble(0.5D, "damage-rate", "damageRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityHurt(final DeEntityHurtEvent event) {
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity victim = event.getEntity();
        if (!hasPermission(victim)) {
            return;
        }
        // Only trigger when damaged by an entity
        if (!(event.getDelegate() instanceof EntityDamageByEntityEvent damageByEntity)) {
            return;
        }
        final Entity damager = damageByEntity.getDamager();
        if (damager.equals(victim)) {
            return;
        }
        final double chance = Math.min(1.0D, level * chanceRate);
        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }
        // Deal extra damage to self
        final double extraDamage = event.getDelegate().getDamage() * (level * damageRate);
        if (extraDamage > 0D) {
            // Apply additional damage
            event.getDelegate().setDamage(event.getDelegate().getDamage() + extraDamage);
        }
    }
}
