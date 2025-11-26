package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;

/**
 * Applies curse translation logic to generated loot tables.
 */
public final class ChestLootController implements Listener {

    private final EnchantTools enchantTools;

    public ChestLootController(final EnchantTools enchantTools) {
        this.enchantTools = enchantTools;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLootGenerate(final LootGenerateEvent event) {
        event.getLoot().forEach(enchantTools::translateEnchantsByChance);
    }
}
