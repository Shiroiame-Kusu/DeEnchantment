package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityShootBowEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Reduces arrow damage and speed when fired with the cursed bow.
 */
public final class PowerCurseHandler extends AbstractCurseHandler {

    private final double damageRate;

    public PowerCurseHandler(final JavaPlugin plugin,
                              final ConfigService configService,
                              final EnchantTools enchantTools,
                              final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.damageRate = Math.max(0D, configDouble(0.15D, "damage-rate", "damageRate"));
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
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) {
            return;
        }
        final double reduction = Math.min(1.0D, Math.max(0D, level * damageRate));
        final double multiplier = Math.max(0D, 1.0D - reduction);
        arrow.setDamage(arrow.getDamage() * multiplier);
        arrow.setVelocity(arrow.getVelocity().multiply(multiplier));
    }
}
