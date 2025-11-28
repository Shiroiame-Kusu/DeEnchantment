package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Recreates the legacy Frost Walker behaviour by temporarily solidifying lava around the wearer.
 */
public final class FrostWalkerCurseHandler extends AbstractCurseHandler {

    private static final int MAX_RADIUS = 32;

    private final Map<UUID, Integer> trackedLevels = new ConcurrentHashMap<>();
    private final Map<Block, BukkitTask> activeBlocks = new ConcurrentHashMap<>();
    private final int radiusRate;
    private final long existTicks;

    public FrostWalkerCurseHandler(final JavaPlugin plugin,
                                   final ConfigService configService,
                                   final EnchantTools enchantTools,
                                   final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.radiusRate = Math.max(1, configInt(1, "radius-rate", "radiusRate"));
        this.existTicks = Math.max(20L, configInt(160, "exist-time", "existTime"));
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
        final Integer level = trackedLevels.get(player.getUniqueId());
        if (level == null || level <= 0) {
            return;
        }
        if (!shouldAffect(player)) {
            return;
        }
        final Location to = event.getTo();
        if (to == null) {
            return;
        }
        final Location center = to.clone().subtract(0, 1, 0);
        final int radius = Math.min(MAX_RADIUS, Math.max(1, (radiusRate * level) + 1));
        final Set<Block> sphere = collectBlocks(center, radius);
        for (final Block block : sphere) {
            if (canFreeze(block, player)) {
                freeze(block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (!activeBlocks.containsKey(block)) {
            return;
        }
        event.setDropItems(false);
        event.setCancelled(true);
        revert(block);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        final Block block = event.getBlock();
        if (!activeBlocks.containsKey(block)) {
            return;
        }
        event.setCancelled(true);
        revert(block);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        trackedLevels.remove(event.getPlayer().getUniqueId());
    }

    private boolean shouldAffect(final Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }
        final Material standing = player.getLocation().getBlock().getType();
        final Material eye = player.getEyeLocation().getBlock().getType();
        return standing != Material.LAVA && eye != Material.LAVA;
    }

    private boolean canFreeze(final Block block, final Entity entity) {
        if (block == null || block.getType() != Material.LAVA) {
            return false;
        }
        if (!(block.getBlockData() instanceof Levelled levelled) || levelled.getLevel() != 0) {
            return false;
        }
        return block.getWorld().equals(entity.getWorld());
    }

    private void freeze(final Block block) {
        final BukkitTask existing = activeBlocks.remove(block);
        if (existing != null) {
            existing.cancel();
        }
        block.setType(Material.OBSIDIAN, false);
        final BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> revert(block), existTicks);
        activeBlocks.put(block, task);
    }

    private void revert(final Block block) {
        final BukkitTask task = activeBlocks.remove(block);
        if (task != null) {
            task.cancel();
        }
        block.setType(Material.LAVA, true);
    }

    private Set<Block> collectBlocks(final Location center, final int radius) {
        final Set<Block> blocks = new HashSet<>();
        final int radiusSquared = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if ((x * x) + (z * z) > radiusSquared) {
                    continue;
                }
                final Block block = center.getWorld().getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z);
                blocks.add(block);
            }
        }
        return blocks;
    }

    @Override
    public void disable() {
        trackedLevels.clear();
        activeBlocks.forEach((block, task) -> {
            if (task != null) {
                task.cancel();
            }
            block.setType(Material.LAVA, true);
        });
        activeBlocks.clear();
    }
}
