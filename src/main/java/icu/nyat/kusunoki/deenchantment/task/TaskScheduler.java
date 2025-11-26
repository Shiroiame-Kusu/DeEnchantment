package icu.nyat.kusunoki.deenchantment.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Small wrapper around the Bukkit scheduler that keeps track of created tasks
 * so they can all be cancelled safely during shutdown or reload.
 */
public final class TaskScheduler {

    private final JavaPlugin plugin;
    private final List<BukkitTask> tasks = new CopyOnWriteArrayList<>();

    public TaskScheduler(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public BukkitTask runRepeatingAsync(final Runnable task, final long delayTicks, final long periodTicks) {
        final BukkitTask handle = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        tasks.add(handle);
        return handle;
    }

    public BukkitTask runRepeatingSync(final Runnable task, final long delayTicks, final long periodTicks) {
        final BukkitTask handle = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        tasks.add(handle);
        return handle;
    }

    public void cancelAll() {
        for (final BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }
}
