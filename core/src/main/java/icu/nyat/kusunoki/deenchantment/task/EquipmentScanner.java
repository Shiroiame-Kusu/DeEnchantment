package icu.nyat.kusunoki.deenchantment.task;

import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Periodically checks player armor slots and fires {@link DePlayerEquipmentChangeEvent}
 * whenever a difference is detected. This mirrors the legacy EquipmentScanner runnable.
 */
public final class EquipmentScanner {

    private static final long PERIOD_TICKS = 10L;

    private final TaskScheduler scheduler;
    private final Map<UUID, ItemStack[]> snapshots = new ConcurrentHashMap<>();

    private BukkitTask task;

    public EquipmentScanner(final TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void start() {
        if (task != null) {
            return;
        }
    task = scheduler.runRepeatingSync(this::scan, 0L, PERIOD_TICKS);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        snapshots.clear();
    }

    private void scan() {
        Bukkit.getOnlinePlayers().forEach(this::inspectPlayer);
        snapshots.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }

    private void inspectPlayer(final Player player) {
        final ItemStack[] armor = sanitize(player.getEquipment().getArmorContents());
        final ItemStack[] previous = snapshots.get(player.getUniqueId());
        if (previous != null && Arrays.deepEquals(armor, previous)) {
            return;
        }
        final DePlayerEquipmentChangeEvent event = new DePlayerEquipmentChangeEvent(player, armor);
        Bukkit.getPluginManager().callEvent(event);
        final ItemStack[] updated = sanitize(event.getArmors());
        snapshots.put(player.getUniqueId(), cloneContents(updated));
        player.getEquipment().setArmorContents(updated);
    }

    private ItemStack[] sanitize(final ItemStack[] source) {
        if (source == null) {
            return new ItemStack[0];
        }
        final ItemStack[] copy = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            final ItemStack original = source[i];
            copy[i] = original == null ? null : original.clone();
        }
        return copy;
    }

    private ItemStack[] cloneContents(final ItemStack[] items) {
        if (items == null) {
            return new ItemStack[0];
        }
        final ItemStack[] copy = Arrays.copyOf(items, items.length);
        for (int i = 0; i < copy.length; i++) {
            final ItemStack item = copy[i];
            copy[i] = item == null ? null : item.clone();
        }
        return copy;
    }
}
