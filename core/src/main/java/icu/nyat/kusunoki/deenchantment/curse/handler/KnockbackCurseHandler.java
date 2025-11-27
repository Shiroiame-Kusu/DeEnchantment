package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityAttackEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * Applies a reverse knockback to the attacker when they strike a target.
 */
public final class KnockbackCurseHandler extends AbstractCurseHandler {

    private final double knockbackRate;

    public KnockbackCurseHandler(final JavaPlugin plugin,
                                 final ConfigService configService,
                                 final EnchantTools enchantTools,
                                 final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.knockbackRate = Math.max(0D, configDouble(0.6D, "knockback-rate", "knockBackRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityAttack(final DeEntityAttackEvent event) {
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity attacker = event.getEntity();
        if (!hasPermission(attacker)) {
            return;
        }
        final Vector direction = attacker.getLocation().getDirection();
        if (direction.lengthSquared() == 0) {
            return;
        }
        final Vector knockback = direction.normalize().multiply(-Math.max(0D, level * knockbackRate));
        attacker.setVelocity(attacker.getVelocity().add(knockback));
    }
}
