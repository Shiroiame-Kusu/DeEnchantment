package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Causes underwater block breaking to fail based on the configured probability.
 */
public final class AquaAffinityCurseHandler extends AbstractCurseHandler {

    private final double failureRatePercent;

    public AquaAffinityCurseHandler(final JavaPlugin plugin,
                                    final ConfigService configService,
                                    final EnchantTools enchantTools,
                                    final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.failureRatePercent = Math.max(0D, configDouble(10.0D, "failure-rate", "rate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (!hasPermission(player)) {
            return;
        }
        final int level = getArmorLevel(player);
        if (level <= 0) {
            return;
        }
        if (!isSubmerged(player)) {
            return;
        }
        final double chance = Math.min(1.0D, (failureRatePercent * level) / 100.0D);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            event.setCancelled(true);
        }
    }

    private boolean isSubmerged(final Player player) {
        final Block eyeBlock = player.getEyeLocation().getBlock();
        final Material type = eyeBlock.getType();
        return type == Material.WATER || type == Material.BUBBLE_COLUMN;
    }
}
