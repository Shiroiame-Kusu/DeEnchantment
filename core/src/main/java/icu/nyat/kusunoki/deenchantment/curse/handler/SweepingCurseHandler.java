package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityAttackEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * Pulls sweep-attack victims toward the attacker.
 */
public final class SweepingCurseHandler extends AbstractCurseHandler {

    private final double powerRate;

    public SweepingCurseHandler(final JavaPlugin plugin,
                                 final ConfigService configService,
                                 final EnchantTools enchantTools,
                                 final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.powerRate = Math.max(0D, configDouble(0.2D, "power-rate", "powerRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityAttack(final DeEntityAttackEvent event) {
        if (event.getDelegate().getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            return;
        }
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity attacker = event.getEntity();
        if (!hasPermission(attacker)) {
            return;
        }
        final Entity target = event.getDelegate().getEntity();
        if (target == null) {
            return;
        }
        final Vector targetVector = target.getLocation().toVector();
        final Vector attackerVector = attacker.getLocation().toVector();
        Vector pull = attackerVector.clone().subtract(targetVector);
        if (pull.lengthSquared() == 0D) {
            return;
        }
        final double strength = Math.max(0D, level * powerRate);
        if (strength <= 0D) {
            return;
        }
        pull = pull.normalize().multiply(strength);
        target.setVelocity(pull);
    }
}
