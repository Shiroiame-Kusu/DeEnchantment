package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.EntityEquipment;

/**
 * Converts reward drops (piglin barters, villager hero gifts, etc.) into cursed variants.
 */
public final class RewardDropController implements Listener {

    private final EnchantTools enchantTools;

    public RewardDropController(final EnchantTools enchantTools) {
        this.enchantTools = enchantTools;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDropItem(final EntityDropItemEvent event) {
        if (shouldSkip(event.getEntity())) {
            return;
        }
        enchantTools.translateEnchantsByChance(event.getItemDrop().getItemStack());
    }

    private boolean shouldSkip(final org.bukkit.entity.Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        final EntityEquipment equipment = mob.getEquipment();
        if (equipment == null) {
            return false;
        }
        return equipment.getItemInMainHandDropChance() == 1.0F
                || equipment.getItemInOffHandDropChance() == 1.0F
                || equipment.getHelmetDropChance() == 1.0F
                || equipment.getChestplateDropChance() == 1.0F
                || equipment.getLeggingsDropChance() == 1.0F
                || equipment.getBootsDropChance() == 1.0F;
    }
}
