package icu.nyat.kusunoki.deenchantment.util.projectile;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * Repeatedly launches projectiles from an entity, optionally consuming ammunition each shot.
 */
public final class ProjectileShooterTask extends BukkitRunnable {

    private final LivingEntity shooter;
    private final Projectile prototype;
    private final double speed;
    private final boolean consumeAmmo;
    private final Material consumeType;
    private int remaining;

    public ProjectileShooterTask(final LivingEntity shooter,
                                 final Projectile prototype,
                                 final int count,
                                 final boolean consumeAmmo,
                                 final Material consumeType) {
        this.shooter = Objects.requireNonNull(shooter, "shooter");
        this.prototype = Objects.requireNonNull(prototype, "prototype");
        this.consumeAmmo = consumeAmmo;
        this.consumeType = consumeType;
        this.remaining = Math.max(0, count);
        final Vector velocity = prototype.getVelocity();
        this.speed = velocity.length();
    }

    @Override
    public void run() {
        if (remaining <= 0) {
            cancel();
            return;
        }
        if (consumeAmmo && shooter instanceof Player player) {
            if (!takeAmmo(player)) {
                cancel();
                return;
            }
        }
        final Vector direction = shooter.getLocation().getDirection();
        if (direction.lengthSquared() == 0D) {
            cancel();
            return;
        }
        shooter.launchProjectile(prototype.getClass(), direction.normalize().multiply(speed));
        remaining--;
        if (remaining <= 0) {
            cancel();
        }
    }

    private boolean takeAmmo(final Player player) {
        if (consumeType == null) {
            return true;
        }
        final PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (stack == null || stack.getType() != consumeType) {
                continue;
            }
            final int amount = stack.getAmount() - 1;
            if (amount <= 0) {
                inventory.setItem(slot, null);
            } else {
                stack.setAmount(amount);
            }
            return true;
        }
        return false;
    }
}
