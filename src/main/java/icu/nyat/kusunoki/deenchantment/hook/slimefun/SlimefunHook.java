package icu.nyat.kusunoki.deenchantment.hook.slimefun;

import icu.nyat.kusunoki.deenchantment.hook.PluginHook;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import io.github.thebusybiscuit.slimefun4.api.events.AsyncMachineOperationFinishEvent;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.AutoDisenchanter;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.AutoEnchanter;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Synchronises lore for Slimefun's auto enchant/disenchant machines so that curse metadata
 * injected by DeEnchantment always matches Paper items produced by those machines.
 */
public final class SlimefunHook implements PluginHook, Listener {

    private final JavaPlugin plugin;
    private final PluginLogger logger;
    private final EnchantTools enchantTools;
    private boolean active;

    public SlimefunHook(final JavaPlugin plugin,
                        final PluginLogger logger,
                        final EnchantTools enchantTools) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.enchantTools = Objects.requireNonNull(enchantTools, "enchantTools");
    }

    @Override
    public String name() {
        return "Slimefun";
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        active = true;
        logger.info("Detected Slimefun; auto machine lore sync enabled");
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMachineFinish(final AsyncMachineOperationFinishEvent event) {
        final Object processor = event.getProcessor();
        if (processor == null) {
            return;
        }
        final Object owner = resolveOwner(processor);
        if (owner instanceof AutoDisenchanter disenchanter) {
            refreshOutputs(disenchanter.getOutputSlots(), event);
        } else if (owner instanceof AutoEnchanter enchanter) {
            refreshOutputs(enchanter.getOutputSlots(), event);
        }
    }

    private Object resolveOwner(final Object processor) {
        try {
            return processor.getClass().getMethod("getOwner").invoke(processor);
        } catch (final ReflectiveOperationException exception) {
            logger.debug(() -> "Unable to access Slimefun processor owner: " + exception.getMessage());
            return null;
        }
    }

    private void refreshOutputs(final int[] outputSlots, final AsyncMachineOperationFinishEvent event) {
        if (outputSlots == null || outputSlots.length == 0) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            final BlockMenu menu = BlockStorage.getInventory(event.getPosition().getBlock());
            if (menu == null) {
                return;
            }
            for (int slot : outputSlots) {
                final ItemStack stack = menu.getItemInSlot(slot);
                if (stack == null) {
                    continue;
                }
                final ItemMeta meta = stack.getItemMeta();
                if (meta == null) {
                    continue;
                }
                enchantTools.updateLore(meta);
                stack.setItemMeta(meta);
                menu.replaceExistingItem(slot, stack);
            }
        });
    }
}
