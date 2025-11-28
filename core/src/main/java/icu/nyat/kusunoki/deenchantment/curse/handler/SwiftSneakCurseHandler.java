package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies slowness and resistance potion effects based on curse level.
 */
public final class SwiftSneakCurseHandler extends AbstractCurseHandler {

    private static final int EFFECT_DURATION_TICKS = 220;
    private static final long TASK_PERIOD_TICKS = 200L;

    private final int slowLevelRate;
    private final int maxSlowLevel;
    private final int resistanceLevelRate;
    private final int maxResistanceLevel;

    private final Map<UUID, PotionTask> slowTasks = new HashMap<>();
    private final Map<UUID, PotionTask> resistanceTasks = new HashMap<>();

    public SwiftSneakCurseHandler(final JavaPlugin plugin,
                                  final ConfigService configService,
                                  final EnchantTools enchantTools,
                                  final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.slowLevelRate = Math.max(0, configInt(1, "slow-level-rate", "slowLevelRate"));
        this.maxSlowLevel = configInt(-1, "max-slow-level", "maxSlowLevel");
        this.resistanceLevelRate = Math.max(0, configInt(1, "resistance-level-rate", "resistanceLevelRate"));
        this.maxResistanceLevel = configInt(3, "max-resistance-level", "maxResistanceLevel");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEquipmentChange(final DePlayerEquipmentChangeEvent event) {
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();
        final int level = getLevel(event);
        final boolean permitted = level > 0 && hasPermission(player);

        if (!permitted) {
            cancelTask(slowTasks.remove(playerId));
            cancelTask(resistanceTasks.remove(playerId));
            return;
        }

        if (maxSlowLevel == 0 || slowLevelRate == 0) {
            cancelTask(slowTasks.remove(playerId));
        } else {
            final int slowLevel = clampLevel(level * slowLevelRate, maxSlowLevel);
            if (slowLevel > 0) {
                slowTasks.compute(playerId, (uuid, task) -> schedulePotionTask(task, player, PotionEffectType.SLOW, slowLevel,
                        slowTasks));
            } else {
                cancelTask(slowTasks.remove(playerId));
            }
        }

        if (maxResistanceLevel == 0 || resistanceLevelRate == 0) {
            cancelTask(resistanceTasks.remove(playerId));
        } else {
            final int resistanceLevel = clampLevel(level * resistanceLevelRate, maxResistanceLevel);
            if (resistanceLevel > 0) {
                resistanceTasks.compute(playerId, (uuid, task) -> schedulePotionTask(task, player,
                        PotionEffectType.DAMAGE_RESISTANCE, resistanceLevel, resistanceTasks));
            } else {
                cancelTask(resistanceTasks.remove(playerId));
            }
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final UUID playerId = event.getPlayer().getUniqueId();
        cancelTask(slowTasks.remove(playerId));
        cancelTask(resistanceTasks.remove(playerId));
    }

    private PotionTask schedulePotionTask(final PotionTask existing,
                                          final Player player,
                                          final PotionEffectType type,
                                          final int level,
                                          final Map<UUID, PotionTask> registry) {
        if (existing == null) {
            final PotionTask task = new PotionTask(player, type, level, registry);
            task.runTaskTimer(plugin, 0L, TASK_PERIOD_TICKS);
            return task;
        }
        existing.setLevel(level);
        return existing;
    }

    private int clampLevel(final int value, final int maxLevel) {
        if (value <= 0) {
            return 0;
        }
        if (maxLevel < 0) {
            return value;
        }
        return Math.min(value, maxLevel);
    }

    private void cancelTask(final PotionTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void disable() {
        slowTasks.values().forEach(BukkitRunnable::cancel);
        resistanceTasks.values().forEach(BukkitRunnable::cancel);
        slowTasks.clear();
        resistanceTasks.clear();
    }

    private final class PotionTask extends BukkitRunnable {

        private final Player player;
        private final PotionEffectType type;
        private final Map<UUID, PotionTask> registry;
        private final UUID playerId;
        private int level;

        private PotionTask(final Player player,
                           final PotionEffectType type,
                           final int level,
                           final Map<UUID, PotionTask> registry) {
            this.player = player;
            this.type = type;
            this.level = Math.max(0, level);
            this.registry = registry;
            this.playerId = player.getUniqueId();
        }

        @Override
        public void run() {
            if (!player.isOnline() || player.isDead()) {
                cancel();
                return;
            }
            if (level <= 0) {
                player.removePotionEffect(type);
                return;
            }
            final PotionEffect effect = new PotionEffect(type, EFFECT_DURATION_TICKS, Math.max(0, level - 1), true, false,
                    false);
            player.addPotionEffect(effect);
        }

        public void setLevel(final int level) {
            this.level = Math.max(0, level);
        }

        @Override
        public void cancel() {
            super.cancel();
            player.removePotionEffect(type);
            registry.remove(playerId, this);
        }
    }
}
