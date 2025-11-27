package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Item;

import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomly deletes additional block drops produced by cursed tools.
 */
public final class FortuneCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;

    public FortuneCurseHandler(final JavaPlugin plugin,
                                final ConfigService configService,
                                final EnchantTools enchantTools,
                                final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, Math.min(1D, configDouble(0.05D, "chance-rate", "chanceRate")));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockDrop(final BlockDropItemEvent event) {
        final java.util.List<Item> drops = event.getItems();
        if (drops.isEmpty()) {
            return;
        }
        final BlockState state = event.getBlockState();
        if (state instanceof BlockInventoryHolder) {
            return;
        }
        if (drops.size() == 1 && drops.get(0).getItemStack().getAmount() == 1
            && drops.get(0).getItemStack().getType() == state.getType()) {
            return;
        }
        final Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        final int level = getLevel(mainHand);
        if (level <= 0) {
            return;
        }
        if (!hasPermission(player)) {
            return;
        }
        final double chance = Math.min(1.0D, level * chanceRate);
        if (chance <= 0D) {
            return;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final ListIterator<Item> iterator = drops.listIterator(drops.size());
        while (iterator.hasPrevious()) {
            final Item drop = iterator.previous();
            if (random.nextDouble() <= chance) {
                iterator.remove();
            }
        }
    }
}
