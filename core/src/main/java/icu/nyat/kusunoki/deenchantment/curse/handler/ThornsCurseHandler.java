package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Periodically damages sprinting players wearing the cursed Thorns armor.
 */
public final class ThornsCurseHandler extends AbstractCurseHandler {

    private final Map<UUID, ThornsMonitor> tasks = new ConcurrentHashMap<>();
    private final double chanceRate;
    private final double damageRate;

    public ThornsCurseHandler(final JavaPlugin plugin,
                              final ConfigService configService,
                              final EnchantTools enchantTools,
                              final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, configDouble(0.05D, "chance-rate", "chanceRate"));
        this.damageRate = Math.max(0D, configDouble(0.5D, "damage-rate", "damageRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEquipmentChange(final DePlayerEquipmentChangeEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (!hasPermission(player)) {
            removeTask(uuid);
            return;
        }
        final int level = getLevel(event);
        if (level <= 0) {
            removeTask(uuid);
            return;
        }
        final ThornsMonitor monitor = tasks.computeIfAbsent(uuid, key -> {
            final ThornsMonitor runner = new ThornsMonitor(player);
            runner.runTaskTimerAsynchronously(plugin, 0L, 10L);
            return runner;
        });
        monitor.setLevel(level);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        removeTask(event.getPlayer().getUniqueId());
    }

    private void removeTask(final UUID uuid) {
        final ThornsMonitor monitor = tasks.remove(uuid);
        if (monitor != null) {
            monitor.cancel();
        }
    }

    @Override
    public void disable() {
        tasks.values().forEach(BukkitRunnable::cancel);
        tasks.clear();
    }

    private final class ThornsMonitor extends BukkitRunnable {

        private final Player player;
        private volatile double chance;
        private volatile double damage;

        private ThornsMonitor(final Player player) {
            this.player = player;
        }

        private void setLevel(final int level) {
            this.chance = Math.min(1.0D, chanceRate * level);
            this.damage = Math.max(0D, damageRate * level);
        }

        @Override
        public void run() {
            if (!player.isOnline()) {
                removeTask(player.getUniqueId());
                cancel();
                return;
            }
            if (!player.isSprinting()) {
                return;
            }
            if (ThreadLocalRandom.current().nextDouble() >= chance) {
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline() || player.isDead()) {
                    return;
                }
                player.damage(damage, player);
            });
        }
    }
}
