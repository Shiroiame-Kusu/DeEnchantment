package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityAttackEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Reduces the outgoing melee damage of the attacker.
 */
public final class SharpnessCurseHandler extends AbstractCurseHandler {

    private final double damageRate;

    public SharpnessCurseHandler(final JavaPlugin plugin,
                                 final ConfigService configService,
                                 final EnchantTools enchantTools,
                                 final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.damageRate = Math.max(0D, configDouble(0.5D, "damage-rate", "damageRate"));
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
        final EntityDamageByEntityEvent delegate = event.getDelegate();
        final double reduced = Math.max(0D, delegate.getDamage() - (damageRate * level));
        delegate.setDamage(reduced);
    }
}
