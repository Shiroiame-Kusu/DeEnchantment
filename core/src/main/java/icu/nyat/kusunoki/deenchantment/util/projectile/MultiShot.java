package icu.nyat.kusunoki.deenchantment.util.projectile;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility for spawning additional projectiles that mimic Minecraft's multishot behaviour.
 */
public final class MultiShot {

    private final LivingEntity shooter;
    private final int level;
    private final Projectile prototype;
    private final double angle;
    private final double speed;
    private final Vector baseDirection;

    public MultiShot(final LivingEntity shooter,
                     final int level,
                     final Projectile prototype,
                     final double angle) {
        this.shooter = Objects.requireNonNull(shooter, "shooter");
        this.level = Math.max(0, level);
        this.prototype = Objects.requireNonNull(prototype, "prototype");
        this.angle = Math.max(0D, angle);
        final Vector velocity = prototype.getVelocity();
        this.speed = velocity.length();
        final Vector direction = shooter.getEyeLocation().getDirection();
        if (direction.lengthSquared() == 0) {
            this.baseDirection = velocity.lengthSquared() == 0 ? new Vector(0, 0, 1) : velocity.clone().normalize();
        } else {
            this.baseDirection = direction.normalize();
        }
    }

    /**
     * Spawns projectiles alternating to the left/right of the player's aim direction.
     */
    public void sweepType() {
        if (level <= 0 || angle <= 0) {
            return;
        }
        final Vector axis = horizontalAxis();
        double accumulatedAngle = 0D;
        for (int n = 1; n <= level; n++) {
            accumulatedAngle += angle;
            final double rotation = (n % 2 == 0 ? -accumulatedAngle : accumulatedAngle);
            final Vector rotated = baseDirection.clone().multiply(speed).rotateAroundAxis(axis, rotation);
            launch(rotated);
        }
    }

    /**
     * Spawns projectiles in random directions within the configured angle cone.
     */
    public void randomType() {
        if (level <= 0 || angle <= 0) {
            return;
        }
        for (int n = 0; n < level; n++) {
            final Vector rotated = baseDirection.clone().multiply(speed);
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            rotated.rotateAroundX(randomRange(random));
            rotated.rotateAroundY(randomRange(random));
            rotated.rotateAroundZ(randomRange(random));
            launch(rotated);
        }
    }

    private double randomRange(final ThreadLocalRandom random) {
        return (random.nextDouble() - 0.5D) * 2D * angle;
    }

    private Vector horizontalAxis() {
        Vector axis = new Vector(0, 1, 0);
        if (Math.abs(baseDirection.dot(axis)) >= 0.999D) {
            axis = new Vector(1, 0, 0);
        }
        return axis.normalize();
    }

    private void launch(final Vector velocity) {
        final Projectile spawned = shooter.launchProjectile(prototype.getClass(), velocity);
        replicateProperties(spawned);
    }

    private void replicateProperties(final Projectile spawned) {
        spawned.setFireTicks(prototype.getFireTicks());
        if (prototype instanceof AbstractArrow sourceArrow && spawned instanceof AbstractArrow arrow) {
            arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
            arrow.setDamage(sourceArrow.getDamage());
            arrow.setCritical(sourceArrow.isCritical());
        }
    }
}
