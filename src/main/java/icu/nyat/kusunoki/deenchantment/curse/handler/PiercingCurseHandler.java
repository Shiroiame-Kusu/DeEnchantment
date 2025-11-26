package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityShootBowEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Causes cursed projectiles to rebound toward their shooter when striking other entities.
 */
public final class PiercingCurseHandler extends AbstractCurseHandler {

    private final double reboundRate;
    private final boolean allowReduce;
    private final Map<UUID, Integer> projectileLevels = new ConcurrentHashMap<>();

    public PiercingCurseHandler(final JavaPlugin plugin,
                                final ConfigService configService,
                                final EnchantTools enchantTools,
                                final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.reboundRate = Math.max(0D, configDouble(0.25D, "rebound-rate", "reboundRate"));
        this.allowReduce = configBoolean(true, "allow-reduce", "allowReduce");
    }

    @EventHandler(ignoreCancelled = true)
    public void onShoot(final DeEntityShootBowEvent event) {
        final Projectile projectile = event.getProjectile();
        if (projectile == null) {
            return;
        }
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity shooter = event.getEntity();
        if (!hasPermission(shooter)) {
            return;
        }
        projectileLevels.put(projectile.getUniqueId(), level);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(final ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();
        final Integer level = projectileLevels.remove(projectile.getUniqueId());
        if (level == null || level <= 0) {
            return;
        }
        if (reboundRate <= 0D) {
            return;
        }
        final Entity hitEntity = event.getHitEntity();
        if (!(hitEntity instanceof ProjectileSource source)) {
            return;
        }
        final Vector incoming = projectile.getVelocity();
        if (incoming.lengthSquared() == 0D) {
            return;
        }
        final Vector rebound = incoming.normalize().multiply(-1 * reboundRate * level);
        final Projectile reboundProjectile = source.launchProjectile(projectile.getClass(), rebound);
        if (projectile instanceof AbstractArrow original && reboundProjectile instanceof AbstractArrow launched) {
            launched.setPickupStatus(original.getPickupStatus());
        }
        reboundProjectile.setShooter(source);
        reboundProjectile.setFireTicks(projectile.getFireTicks());
        final int nextLevel = allowReduce ? level - 1 : level;
        if (nextLevel > 0) {
            projectileLevels.put(reboundProjectile.getUniqueId(), nextLevel);
        }
        projectile.remove();
    }

    @Override
    public void disable() {
        projectileLevels.clear();
    }
}
