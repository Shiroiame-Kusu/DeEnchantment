package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerFishEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Player;
import org.bukkit.entity.FishHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Extends fishing wait times for cursed rods.
 */
public final class LureCurseHandler extends AbstractCurseHandler {

    private final double waitTimeRate;

    public LureCurseHandler(final JavaPlugin plugin,
                            final ConfigService configService,
                            final EnchantTools enchantTools,
                            final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.waitTimeRate = Math.max(0D, configDouble(0.2D, "wait-time-rate", "waitTimeRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(final DePlayerFishEvent event) {
        final PlayerFishEvent delegate = event.getDelegate();
        if (delegate.getState() != PlayerFishEvent.State.FISHING) {
            return;
        }
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final Player player = event.getPlayer();
        if (!hasPermission(player)) {
            return;
        }
        FishHook hook = delegate.getHook();

        if (hook == null) {
            return;
        }
        final double multiplier = 1.0D + Math.max(0D, level * waitTimeRate);
        hook.setMaxWaitTime((int) Math.round(hook.getMaxWaitTime() * multiplier));
        hook.setMinWaitTime((int) Math.round(hook.getMinWaitTime() * multiplier));
    }
}
