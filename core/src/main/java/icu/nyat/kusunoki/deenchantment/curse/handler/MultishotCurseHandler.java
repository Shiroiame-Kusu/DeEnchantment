package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityShootBowEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.projectile.ProjectileShooterTask;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Forces cursed ranged weapons to keep firing consecutive shots.
 */
public final class MultishotCurseHandler extends AbstractCurseHandler {

    private final long periodTicks;

    public MultishotCurseHandler(final JavaPlugin plugin,
                                 final ConfigService configService,
                                 final EnchantTools enchantTools,
                                 final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.periodTicks = Math.max(1L, configInt(3, "period"));
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
        Projectile projectile = event.getProjectile();

        if (projectile == null) {
            return;
        }
        final EntityShootBowEvent delegate = event.getDelegate();
        final boolean consumeAmmo = shouldConsumeAmmo(shooter, delegate);
        final Material ammoType = consumeAmmo && delegate.getConsumable() != null
                ? delegate.getConsumable().getType()
                : null;
        final ProjectileShooterTask task = new ProjectileShooterTask(shooter, projectile, level, consumeAmmo, ammoType);
        task.runTaskTimer(plugin, periodTicks, periodTicks);
    }

    private boolean shouldConsumeAmmo(final LivingEntity shooter, final EntityShootBowEvent event) {
        if (!(shooter instanceof Player player)) {
            return false;
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }
        return event.getConsumable() != null && event.getConsumable().getType() != Material.AIR;
    }
}
