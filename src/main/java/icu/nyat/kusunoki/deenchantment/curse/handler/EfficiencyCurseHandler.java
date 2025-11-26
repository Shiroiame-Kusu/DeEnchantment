package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeBreakBlockEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Cancels block breaking attempts with a configurable probability based on level.
 */
public final class EfficiencyCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;

    public EfficiencyCurseHandler(final JavaPlugin plugin,
                                  final ConfigService configService,
                                  final EnchantTools enchantTools,
                                  final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, configDouble(0.08D, "chance-rate", "rate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final DeBreakBlockEvent event) {
        final int level = event.getCurseLevel(curse);
        if (level <= 0) {
            return;
        }
        if (!hasPermission(event.getPlayer())) {
            return;
        }
        final double chance = Math.min(1.0D, chanceRate * level);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            event.setCancelled(true);
        }
    }
}
