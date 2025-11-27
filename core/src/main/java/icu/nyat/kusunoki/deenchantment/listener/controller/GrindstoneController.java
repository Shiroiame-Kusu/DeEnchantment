package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Removes custom lore data when items are purified through a grindstone.
 */
public final class GrindstoneController implements Listener {

    private final EnchantTools enchantTools;

    public GrindstoneController(final EnchantTools enchantTools) {
        this.enchantTools = enchantTools;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGrindstone(final InventoryClickEvent event) {
        final InventoryView view = event.getWhoClicked().getOpenInventory();
        if (view.getType() != InventoryType.GRINDSTONE) {
            return;
        }
        final ItemStack result = view.getItem(2);
        if (result == null || result.getType().isAir()) {
            return;
        }
        final ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            enchantTools.clearEnchantLore(meta);
            result.setItemMeta(meta);
            view.setItem(2, result);
        }
    }
}
