package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Causes players to consume oxygen faster when underwater.
 */
public final class RespirationCurseHandler extends AbstractCurseHandler {

    private final double speedRate;
    private final Map<Player, Integer> trackedLevels = new ConcurrentHashMap<>();

    public RespirationCurseHandler(final JavaPlugin plugin,
                                    final ConfigService configService,
                                    final EnchantTools enchantTools,
                                    final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.speedRate = configDouble(1.0D, "speed-rate", "speedRate");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEquipmentChange(final DePlayerEquipmentChangeEvent event) {
        final int level = getLevel(event);
        final Player player = event.getPlayer();
        if (level <= 0 || !hasPermission(player)) {
            trackedLevels.remove(player);
            return;
        }
        trackedLevels.put(player, level);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAirChange(final EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!isAffected(player)) {
            return;
        }
        final int level = trackedLevels.getOrDefault(player, 0);
        if (level <= 0) {
            return;
        }
        if (player.getEyeLocation().getBlock().getType() != Material.WATER) {
            return;
        }
        final int delta = (int) Math.round(speedRate * level);
        event.setAmount(Math.max(-20, event.getAmount() - delta));
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        trackedLevels.remove(event.getPlayer());
    }

    private boolean isAffected(final Player player) {
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return false;
        }
        return hasPermission(player);
    }

    @Override
    public void disable() {
        trackedLevels.clear();
    }
}
