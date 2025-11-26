package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies a slowness effect while the player swims, reducing underwater mobility and vision.
 */
public final class DepthStriderCurseHandler extends AbstractCurseHandler {

    private final Map<UUID, Integer> trackedLevels = new ConcurrentHashMap<>();
    private final int effectDurationTicks;
    private final int slownessMultiplier;

    public DepthStriderCurseHandler(final JavaPlugin plugin,
                                    final ConfigService configService,
                                    final EnchantTools enchantTools,
                                    final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.effectDurationTicks = Math.max(40, configInt(120, "effect-duration", "effectDuration"));
        this.slownessMultiplier = Math.max(1, configInt(2, "slowness-multiplier", "rate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEquipmentChange(final DePlayerEquipmentChangeEvent event) {
        final Player player = event.getPlayer();
        if (!hasPermission(player)) {
            trackedLevels.remove(player.getUniqueId());
            return;
        }
        final int level = getLevel(event);
        if (level > 0) {
            trackedLevels.put(player.getUniqueId(), level);
        } else {
            trackedLevels.remove(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (!isAffected(player)) {
            return;
        }
        final Integer level = trackedLevels.get(player.getUniqueId());
        if (level == null || level <= 0) {
            return;
        }
        if (!isSubmerged(player)) {
            return;
        }
        final int amplifier = Math.max(0, (slownessMultiplier * level) - 1);
        final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, effectDurationTicks, amplifier, true, false, true);
        player.addPotionEffect(effect);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        trackedLevels.remove(event.getPlayer().getUniqueId());
    }

    private boolean isAffected(final Player player) {
        return player.getGameMode() != GameMode.SPECTATOR && hasPermission(player);
    }

    private boolean isSubmerged(final Player player) {
        final Material type = player.getEyeLocation().getBlock().getType();
        return type == Material.WATER || type == Material.BUBBLE_COLUMN;
    }

    @Override
    public void disable() {
        trackedLevels.clear();
    }
}
