package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Applies slowness when walking on soul sand or soul soil (curse version of Soul Speed).
 */
public final class SoulSpeedCurseHandler extends AbstractCurseHandler {

    private static final int EFFECT_DURATION = 60;  // 3 seconds

    private final int slowLevelRate;
    private final long periodTicks;
    private final Set<Material> soulBlocks;
    private final Map<UUID, SoulWatcher> watchers = new HashMap<>();

    public SoulSpeedCurseHandler(final JavaPlugin plugin,
                                 final ConfigService configService,
                                 final EnchantTools enchantTools,
                                 final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.slowLevelRate = Math.max(1, configInt(1, "slow-level-rate", "slowLevelRate"));
        this.periodTicks = Math.max(1L, configInt(10, "period"));
        this.soulBlocks = Collections.unmodifiableSet(resolveSoulBlocks());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEquipmentChange(final DePlayerEquipmentChangeEvent event) {
        final Player player = event.getPlayer();
        final int level = getLevel(event);
        final boolean permitted = level > 0 && hasPermission(player);
        if (!permitted) {
            removeWatcher(player);
            return;
        }
        watchers.compute(player.getUniqueId(), (uuid, watcher) -> {
            if (watcher == null) {
                final SoulWatcher created = new SoulWatcher(player, level);
                created.runTaskTimer(plugin, 0L, periodTicks);
                return created;
            }
            watcher.setLevel(level);
            return watcher;
        });
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        removeWatcher(event.getPlayer());
    }

    private Set<Material> resolveSoulBlocks() {
        final ConfigurationSection section = configuration();
        final List<String> configured = section == null ? List.of() : section.getStringList("blocks");
        final List<String> sources = configured == null || configured.isEmpty()
                ? List.of("SOUL_SAND", "SOUL_SOIL")
                : configured;
        final Set<Material> materials = EnumSet.noneOf(Material.class);
        for (final String name : sources) {
            if (name == null || name.isEmpty()) {
                continue;
            }
            final Material material = Material.matchMaterial(name);
            if (material != null) {
                materials.add(material);
            }
        }
        if (materials.isEmpty()) {
            materials.add(Material.SOUL_SAND);
            materials.add(Material.SOUL_SOIL);
        }
        return materials;
    }

    private void removeWatcher(final Player player) {
        if (player == null) {
            return;
        }
        final SoulWatcher watcher = watchers.remove(player.getUniqueId());
        if (watcher != null) {
            watcher.cancel();
        }
    }

    @Override
    public void disable() {
        watchers.values().forEach(BukkitRunnable::cancel);
        watchers.clear();
    }

    private final class SoulWatcher extends BukkitRunnable {

        private final Player player;
        private volatile int level;

        private SoulWatcher(final Player player, final int level) {
            this.player = player;
            this.level = level;
        }

        private void setLevel(final int level) {
            this.level = level;
        }

        @Override
        public void run() {
            if (!player.isOnline() || player.isDead()) {
                cancel();
                watchers.remove(player.getUniqueId());
                return;
            }
            // Check if standing on soul blocks
            final Material blockBelow = player.getLocation().clone().subtract(0, 0.5, 0).getBlock().getType();
            if (!soulBlocks.contains(blockBelow)) {
                return;
            }
            // Apply slowness
            final int amplifier = Math.max(0, (level * slowLevelRate) - 1);
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW,
                    EFFECT_DURATION,
                    amplifier,
                    true,
                    false,
                    true
            ));
        }
    }
}
