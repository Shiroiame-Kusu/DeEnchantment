package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityAttackEvent;
import icu.nyat.kusunoki.deenchantment.util.entity.EntityClassifications;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Reduces outgoing damage when striking arthropod mobs.
 */
public final class BaneOfArthropodsCurseHandler extends AbstractCurseHandler {

    private final double reduceRate;

    public BaneOfArthropodsCurseHandler(final JavaPlugin plugin,
                                        final ConfigService configService,
                                        final EnchantTools enchantTools,
                                        final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.reduceRate = Math.max(0D, configDouble(2.5D, "reduce-rate", "reduceRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityAttack(final DeEntityAttackEvent event) {
        final EntityDamageByEntityEvent delegate = event.getDelegate();
        final LivingEntity victim = asLiving(delegate.getEntity());
        if (!EntityClassifications.isArthropod(victim)) {
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
        final double reduced = Math.max(0D, delegate.getDamage() - (reduceRate * level));
        delegate.setDamage(reduced);
    }

    private LivingEntity asLiving(final org.bukkit.entity.Entity entity) {
        return entity instanceof LivingEntity living ? living : null;
    }
}
