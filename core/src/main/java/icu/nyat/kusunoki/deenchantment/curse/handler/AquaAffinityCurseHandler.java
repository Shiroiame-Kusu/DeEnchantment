package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies mining fatigue when the wearer is underwater, making it harder to break blocks.
 */
public final class AquaAffinityCurseHandler extends AbstractCurseHandler {

    private static final int EFFECT_DURATION = 60;  // 3 seconds

    private final Map<UUID, WaterWatcher> watchers = new ConcurrentHashMap<>();
    private final int fatigueLevelRate;
    private final long checkPeriod;

    public AquaAffinityCurseHandler(final JavaPlugin plugin,
                                    final ConfigService configService,
                                    final EnchantTools enchantTools,
                                    final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.fatigueLevelRate = Math.max(1, configInt(1, "fatigue-level-rate", "fatigueLevelRate"));
        this.checkPeriod = Math.max(10L, configInt(20, "check-period", "checkPeriod"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEquipmentChange(final DePlayerEquipmentChangeEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final int level = getLevel(event);
        if (level <= 0 || !hasPermission(player)) {
            removeWatcher(uuid);
            return;
        }
        watchers.compute(uuid, (key, existing) -> {
            if (existing != null) {
                existing.setLevel(level);
                return existing;
            }
            final WaterWatcher watcher = new WaterWatcher(player, level);
            watcher.runTaskTimer(plugin, 0L, checkPeriod);
            return watcher;
        });
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        removeWatcher(event.getPlayer().getUniqueId());
    }

    private void removeWatcher(final UUID uuid) {
        final WaterWatcher watcher = watchers.remove(uuid);
        if (watcher != null) {
            watcher.cancel();
        }
    }

    @Override
    public void disable() {
        watchers.values().forEach(BukkitRunnable::cancel);
        watchers.clear();
    }

    private boolean isSubmerged(final Player player) {
        final Material type = player.getEyeLocation().getBlock().getType();
        return type == Material.WATER || type == Material.BUBBLE_COLUMN;
    }

    private final class WaterWatcher extends BukkitRunnable {

        private final Player player;
        private volatile int level;

        private WaterWatcher(final Player player, final int level) {
            this.player = player;
            this.level = level;
        }

        private void setLevel(final int level) {
            this.level = level;
        }

        @Override
        public void run() {
            if (!player.isOnline() || player.isDead()) {
                removeWatcher(player.getUniqueId());
                return;
            }
            // Check if player is underwater
            if (!isSubmerged(player)) {
                return;
            }
            // Apply mining fatigue
            final int amplifier = Math.max(0, (level * fatigueLevelRate) - 1);
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_DIGGING,
                    EFFECT_DURATION,
                    amplifier,
                    true,
                    false,
                    true
            ));
        }
    }
}
