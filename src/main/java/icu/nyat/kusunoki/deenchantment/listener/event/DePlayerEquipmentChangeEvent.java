package icu.nyat.kusunoki.deenchantment.listener.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Fired when the periodic equipment scanner detects a change in a player's armor slots.
 */
public final class DePlayerEquipmentChangeEvent extends DeEnchantmentEvent {

    private ItemStack[] armors;

    public DePlayerEquipmentChangeEvent(final Player player, final ItemStack[] armors) {
        super(player, true);
        this.armors = cloneContents(armors);
    }

    public Player getPlayer() {
        return (Player) getEntity();
    }

    public ItemStack[] getArmors() {
        return cloneContents(armors);
    }

    public void setArmors(final ItemStack[] armors) {
        this.armors = cloneContents(armors);
    }

    @Override
    protected void populateCache() {
        collectFromArray(armors);
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
