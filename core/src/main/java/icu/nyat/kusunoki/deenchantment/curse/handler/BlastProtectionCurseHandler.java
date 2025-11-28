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
 * Chance to trigger an explosion centered on the entity when they are damaged.
 */
public final class BlastProtectionCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;
    private final double explosionRate;
    private final boolean allowFire;
    private final boolean allowDestroy;

    public BlastProtectionCurseHandler(final JavaPlugin plugin,
                                        final ConfigService configService,
                                        final EnchantTools enchantTools,
                                        final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = configDouble(0.1D, "chance", "changce");
        this.explosionRate = configDouble(0.5D, "explosion-rate", "explosionRate");
        this.allowFire = configBoolean(false, "allow-fire", "allowFire");
        this.allowDestroy = configBoolean(false, "allow-destroy", "allowDestroy");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityHurt(final DeEntityHurtEvent event) {
        final EntityDamageEvent damageEvent = event.getDelegate();
        final EntityDamageEvent.DamageCause cause = damageEvent.getCause();
        if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity entity = event.getEntity();
        if (!hasPermission(entity)) {
            return;
        }
        final double chance = Math.max(0D, level * chanceRate);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        final float power = (float) Math.max(0.1D, level * explosionRate);
        entity.getWorld().createExplosion(entity.getLocation(), power, allowFire, allowDestroy, entity);
    }
}
