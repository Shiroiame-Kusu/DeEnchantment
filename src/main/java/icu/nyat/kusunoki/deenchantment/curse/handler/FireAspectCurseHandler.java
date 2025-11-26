package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityAttackEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ignites the attacker when they strike an enemy.
 */
public final class FireAspectCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;
    private final int fireTickRate;

    public FireAspectCurseHandler(final JavaPlugin plugin,
                                  final ConfigService configService,
                                  final EnchantTools enchantTools,
                                  final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, configDouble(0.2D, "chance-rate", "chanceRate"));
        this.fireTickRate = Math.max(1, configInt(20, "fire-tick-rate", "fireTickRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityAttack(final DeEntityAttackEvent event) {
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final LivingEntity attacker = event.getEntity();
        if (!hasPermission(attacker)) {
            return;
        }
        final double chance = Math.min(1.0D, level * chanceRate);
        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }
        final int addedTicks = level * fireTickRate;
        attacker.setFireTicks(attacker.getFireTicks() + addedTicks);
    }
}
