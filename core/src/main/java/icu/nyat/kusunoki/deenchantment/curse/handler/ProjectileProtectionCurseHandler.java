package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Recreates the legacy "projectile attraction" curse behaviour. Depending on the mode the curse either
 * pulls nearby projectiles directly toward cursed players or retargets newly launched projectiles toward
 * the closest cursed entity.
 */
public final class ProjectileProtectionCurseHandler extends AbstractCurseHandler {

    private static final double MIN_DIRECTION_LEN_SQUARED = 1.0E-6;
    private static final int MAX_TRACKED_PROJECTILES = 128;

    private final boolean playerOnly;
    private final double radiusMultiplier;
    private final double maxRadius;
    private final long periodTicks;

    private final Map<UUID, CollectorTask> collectorTasks = new ConcurrentHashMap<>();
    private final Map<UUID, TargetFinderTask> projectileTasks = new ConcurrentHashMap<>();

    public ProjectileProtectionCurseHandler(final JavaPlugin plugin,
                                            final ConfigService configService,
                                            final EnchantTools enchantTools,
                                            final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.playerOnly = configBoolean("player-only", true);
        this.radiusMultiplier = Math.max(0D, configDouble(1.0D, "radius-multiplier", "radius"));
        this.maxRadius = Math.max(0D, configDouble(10.0D, "max-radius"));
        this.periodTicks = Math.max(1L, configInt(5, "period"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEquipmentChange(final DePlayerEquipmentChangeEvent event) {
        if (!playerOnly) {
            return;
        }
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();
        cancelCollector(playerId);

        final int level = event.getCurseLevel(curse);
        if (level <= 0) {
            return;
        }
        if (!hasPermission(player)) {
            return;
        }
        final double radius = Math.min(maxRadius, radiusMultiplier * level);
        if (radius <= 0) {
            return;
        }
        final CollectorTask task = new CollectorTask(playerId, radius);
        collectorTasks.put(playerId, task);
        task.runTaskTimer(plugin, 0L, periodTicks);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        if (playerOnly) {
            return;
        }
        if (projectileTasks.size() >= MAX_TRACKED_PROJECTILES) {
            return;
        }
        final Projectile projectile = event.getEntity();
        final UUID projectileId = projectile.getUniqueId();
        if (projectileTasks.containsKey(projectileId)) {
            return;
        }
        final TargetFinderTask task = new TargetFinderTask(projectile);
        projectileTasks.put(projectileId, task);
        task.runTaskTimer(plugin, periodTicks, periodTicks);
    }

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (playerOnly) {
            return;
        }
        stopTracking(event.getEntity(), true);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        cancelCollector(event.getPlayer().getUniqueId());
    }

    private void cancelCollector(final UUID playerId) {
        final CollectorTask existing = collectorTasks.remove(playerId);
        if (existing != null) {
            existing.stop();
        }
    }

    private void stopTracking(final Projectile projectile, final boolean resetGravity) {
        final TargetFinderTask task = projectileTasks.remove(projectile.getUniqueId());
        if (task != null) {
            task.stop(resetGravity);
        } else if (resetGravity) {
            projectile.setGravity(true);
        }
    }

    @Override
    public void disable() {
        collectorTasks.values().forEach(CollectorTask::stop);
        collectorTasks.clear();
        projectileTasks.values().forEach(task -> task.stop(true));
        projectileTasks.clear();
    }

    private final class CollectorTask extends BukkitRunnable {

        private final UUID playerId;
        private final double radius;

        private CollectorTask(final UUID playerId, final double radius) {
            this.playerId = playerId;
            this.radius = radius;
        }

        @Override
        public void run() {
            final Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                stop();
                return;
            }
            if (getArmorLevel(player) <= 0 || !hasPermission(player)) {
                stop();
                return;
            }
            final Location eyeLocation = player.getEyeLocation();
            final Vector eyeVector = eyeLocation.toVector();
            final List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
            for (Entity candidate : nearby) {
                if (!(candidate instanceof Projectile projectile)) {
                    continue;
                }
                if (projectile.isDead() || projectile.isOnGround()) {
                    continue;
                }
                final Vector velocity = projectile.getVelocity();
                final double speed = velocity.length();
                if (speed <= 0) {
                    continue;
                }
                final Vector direction = eyeVector.clone().subtract(projectile.getLocation().toVector());
                if (direction.lengthSquared() <= MIN_DIRECTION_LEN_SQUARED) {
                    continue;
                }
                projectile.setVelocity(direction.normalize().multiply(speed));
            }
        }

        private void stop() {
            cancel();
            collectorTasks.remove(playerId, this);
        }
    }

    private final class TargetFinderTask extends BukkitRunnable {

        private final Projectile projectile;
        private final long startTime = System.currentTimeMillis();

        private TargetFinderTask(final Projectile projectile) {
            this.projectile = projectile;
        }

        @Override
        public void run() {
            if (projectile.isDead() || projectile.isOnGround() || System.currentTimeMillis() - startTime > 10_000L) {
                stop(true);
                return;
            }
            final List<LivingEntity> candidates = gatherCandidates();
            if (candidates.isEmpty()) {
                return;
            }
            final Location location = projectile.getLocation();
            final Vector projectileVector = location.toVector();
            for (LivingEntity living : candidates) {
                final int level = enchantTools.getLevelCount(living, curse) * 2;
                if (level <= 0) {
                    continue;
                }
                final double distance = living.getLocation().distance(location) - 1.0D;
                if (level < distance) {
                    continue;
                }
                final Vector direction = living.getEyeLocation().toVector().subtract(projectileVector.clone());
                if (direction.lengthSquared() <= MIN_DIRECTION_LEN_SQUARED) {
                    continue;
                }
                final double speed = Math.max(0.1D, projectile.getVelocity().length());
                projectile.setGravity(false);
                projectile.setVelocity(direction.normalize().multiply(speed));
                stop(false);
                return;
            }
        }

        private List<LivingEntity> gatherCandidates() {
            final List<LivingEntity> result = new ArrayList<>();
            for (Entity entity : projectile.getNearbyEntities(5.0D, 5.0D, 5.0D)) {
                if (!(entity instanceof LivingEntity living)) {
                    continue;
                }
                final Object shooter = projectile.getShooter();
                if (shooter instanceof LivingEntity shooterLiving && shooterLiving.equals(living)) {
                    continue;
                }
                result.add(living);
            }
            result.sort(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(projectile.getLocation())));
            return result;
        }

        private void stop(final boolean resetGravity) {
            cancel();
            projectileTasks.remove(projectile.getUniqueId(), this);
            if (resetGravity) {
                projectile.setGravity(true);
            }
        }
    }
}
