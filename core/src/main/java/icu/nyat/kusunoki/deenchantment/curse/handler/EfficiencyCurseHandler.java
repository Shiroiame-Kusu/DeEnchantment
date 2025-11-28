package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeBreakBlockEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Applies mining fatigue when breaking blocks with a cursed tool, slowing down mining speed.
 */
public final class EfficiencyCurseHandler extends AbstractCurseHandler {

    private final int fatigueDuration;
    private final int fatigueLevelRate;

    public EfficiencyCurseHandler(final JavaPlugin plugin,
                                  final ConfigService configService,
                                  final EnchantTools enchantTools,
                                  final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.fatigueDuration = Math.max(20, configInt(100, "fatigue-duration", "fatigueDuration"));
        this.fatigueLevelRate = Math.max(0, configInt(1, "fatigue-level-rate", "fatigueLevelRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final DeBreakBlockEvent event) {
        final int level = event.getCurseLevel(curse);
        if (level <= 0) {
            return;
        }
        final Player player = event.getPlayer();
        if (!hasPermission(player)) {
            return;
        }
        // Apply mining fatigue effect - slows down future block breaking
        final int amplifier = Math.max(0, (level * fatigueLevelRate) - 1);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW_DIGGING,
                fatigueDuration,
                amplifier,
                true,
                false,
                true
        ));
    }
}
