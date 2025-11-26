package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityShootBowEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Accelerates arrows fired from cursed bows, making them harder to control.
 */
public final class FlameCurseHandler extends AbstractCurseHandler {

    private final double speedRate;

    public FlameCurseHandler(final JavaPlugin plugin,
                              final ConfigService configService,
                              final EnchantTools enchantTools,
                              final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.speedRate = Math.max(0D, configDouble(0.2D, "speed-rate", "speedRate"));
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
        final Projectile projectile = event.getProjectile();
        if (projectile == null) {
            return;
        }
        final double multiplier = 1.0D + Math.max(0D, level * speedRate);
        if (multiplier == 1.0D) {
            return;
        }
        projectile.setVelocity(projectile.getVelocity().multiply(multiplier));
    }
}
