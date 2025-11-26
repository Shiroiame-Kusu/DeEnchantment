package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityHurtEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Increases fall damage and applies a slowness debuff when triggered.
 */
public final class FeatherFallingCurseHandler extends AbstractCurseHandler {

    private final double damageRate;
    private final int potionTimeRate;
    private final int potionLevelRate;

    public FeatherFallingCurseHandler(final JavaPlugin plugin,
                                       final ConfigService configService,
                                       final EnchantTools enchantTools,
                                       final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.damageRate = configDouble(0.12D, "damage-rate", "damageRate");
        this.potionTimeRate = Math.max(1, configInt(20, "potion-time-rate", "potionTimeRate"));
        this.potionLevelRate = Math.max(1, configInt(1, "potion-level", "potionLevel"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityHurt(final DeEntityHurtEvent event) {
        final EntityDamageEvent delegate = event.getDelegate();
        if (delegate.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity entity = event.getEntity();
        if (!hasPermission(entity)) {
            return;
        }
        final double damage = delegate.getDamage();
        delegate.setDamage(damage + (damage * (level * damageRate)));
        final int duration = Math.max(1, level * potionTimeRate);
        final int amplifier = Math.max(0, (potionLevelRate * level) - 1);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, amplifier, true, false, true));
    }
}
