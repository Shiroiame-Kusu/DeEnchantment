package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityShootBowEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.projectile.MultiShot;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Launches extra projectiles whenever a cursed bow fires.
 */
public final class InfinityCurseHandler extends AbstractCurseHandler {

    private final int type;
    private final double angle;

    public InfinityCurseHandler(final JavaPlugin plugin,
                                 final ConfigService configService,
                                 final EnchantTools enchantTools,
                                 final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.type = Math.max(0, configInt(0, "type"));
        this.angle = Math.max(0D, configDouble(0.2D, "angle"));
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
        final MultiShot multiShot = new MultiShot(shooter, level, projectile, angle);
        if (type == 1) {
            multiShot.randomType();
        } else {
            multiShot.sweepType();
        }
    }
}
