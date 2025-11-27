package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerFishEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Causes caught loot to slip away while fishing.
 */
public final class LuckOfTheSeaCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;
    private final String message;

    public LuckOfTheSeaCurseHandler(final JavaPlugin plugin,
                                    final ConfigService configService,
                                    final EnchantTools enchantTools,
                                    final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, Math.min(1D, configDouble(0.1D, "chance-rate", "chanceRate")));
        this.message = configString("&6你受到了大海的嫌弃并回收了你的东西", "message");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(final DePlayerFishEvent event) {
        final PlayerFishEvent delegate = event.getDelegate();
        if (delegate.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
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
        final double chance = Math.min(1.0D, level * chanceRate);
        if (chance <= 0D || ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        if (delegate.getCaught() != null) {
            delegate.getCaught().remove();
        }
        player.sendMessage(PlaceholderText.apply(player, color(message)));
    }
}
