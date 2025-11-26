package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Applies curse translation to the gear of naturally spawned mobs.
 */
public final class EntitySpawnController implements Listener {

    private final JavaPlugin plugin;
    private final EnchantTools enchantTools;

    public EntitySpawnController(final JavaPlugin plugin, final EnchantTools enchantTools) {
        this.plugin = plugin;
        this.enchantTools = enchantTools;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> apply(event.getEntity().getEquipment()));
    }

    private void apply(final EntityEquipment equipment) {
        if (equipment == null) {
            return;
        }
        translate(equipment.getArmorContents(), equipment::setArmorContents);
        final ItemStack mainHand = equipment.getItemInMainHand();
        if (mainHand != null) {
            enchantTools.translateEnchantsByChance(mainHand);
            equipment.setItemInMainHand(mainHand);
        }
        final ItemStack offHand = equipment.getItemInOffHand();
        if (offHand != null) {
            enchantTools.translateEnchantsByChance(offHand);
            equipment.setItemInOffHand(offHand);
        }
    }

    private void translate(final ItemStack[] source, final java.util.function.Consumer<ItemStack[]> setter) {
        if (source == null) {
            return;
        }
        final ItemStack[] copy = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            final ItemStack stack = source[i];
            if (stack != null && !stack.getType().isAir()) {
                enchantTools.translateEnchantsByChance(stack);
            }
            copy[i] = stack;
        }
        setter.accept(copy);
    }
}
