package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityHurtEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Mirrors the legacy Protection curse: increases the amount of damage taken.
 */
public final class ProtectionCurseHandler extends AbstractCurseHandler {

    private final double damageRate;

    public ProtectionCurseHandler(final JavaPlugin plugin,
                                   final ConfigService configService,
                                   final EnchantTools enchantTools,
                                   final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.damageRate = configDouble(0.04D, "damage-rate", "damageRate");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityHurt(final DeEntityHurtEvent event) {
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        if (!hasPermission(event.getEntity())) {
            return;
        }
        final EntityDamageEvent delegate = event.getDelegate();
        final double base = delegate.getDamage();
        final double total = base + (base * (level * damageRate));
        delegate.setDamage(total);
    }
}
