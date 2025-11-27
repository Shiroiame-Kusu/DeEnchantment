package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityAttackEvent;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityProjectileEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Boosts damage dealt to land mobs when using cursed tridents.
 */
public final class ImpalingCurseHandler extends AbstractCurseHandler {

    private final double meleeDamageRate;
    private final double remoteDamageRate;

    public ImpalingCurseHandler(final JavaPlugin plugin,
                                final ConfigService configService,
                                final EnchantTools enchantTools,
                                final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.meleeDamageRate = Math.max(0D, configDouble(0.5D, "melee-damage-rate", "meleeDamageRate"));
        this.remoteDamageRate = Math.max(0D, configDouble(1.0D, "remote-damage-rate", "remoteDamageRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityAttack(final DeEntityAttackEvent event) {
        final LivingEntity attacker = event.getEntity();
        if (!hasPermission(attacker)) {
            return;
        }
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        applyDamage(event.getDelegate(), level * meleeDamageRate);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileAttack(final DeEntityProjectileEvent event) {
        final LivingEntity attacker = event.getEntity();
        if (!hasPermission(attacker)) {
            return;
        }
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        applyDamage(event.getDelegate(), level * remoteDamageRate);
    }

    private void applyDamage(final EntityDamageByEntityEvent delegate, final double bonusDamage) {
        final Entity target = delegate.getEntity();
        if (target instanceof WaterMob || target instanceof Guardian) {
            return;
        }
        final double bonus = Math.max(0D, bonusDamage);
        if (bonus <= 0D) {
            return;
        }
        delegate.setDamage(delegate.getDamage() + bonus);
    }
}
