package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.LightningStrike;

/**
 * Summons punitive lightning strikes when cursed tridents hit during storms.
 */
public final class ChannelingCurseHandler extends AbstractCurseHandler {

    private final boolean effectOnly;
    private final double extraDamage;

    public ChannelingCurseHandler(final JavaPlugin plugin,
                                  final ConfigService configService,
                                  final EnchantTools enchantTools,
                                  final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.effectOnly = configBoolean(false, "effect-only", "is-effect", "isEffect");
        this.extraDamage = Math.max(0D, configDouble(0D, "extra-damage", "extraDamage"));
    }

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) {
            return;
        }
        if (event.getHitEntity() == null) {
            return;
        }
        if (!trident.getWorld().isThundering()) {
            return;
        }
        if (!(trident.getShooter() instanceof LivingEntity shooter)) {
            return;
        }
        if (!hasPermission(shooter)) {
            return;
        }
        final int level = getLevel(trident.getItem());
        if (level <= 0) {
            return;
        }
        final LightningStrike strike = effectOnly
                ? trident.getWorld().strikeLightningEffect(shooter.getLocation())
                : trident.getWorld().strikeLightning(shooter.getLocation());
        if (extraDamage > 0D) {
            shooter.damage(extraDamage * level, strike);
        }
    }
}
