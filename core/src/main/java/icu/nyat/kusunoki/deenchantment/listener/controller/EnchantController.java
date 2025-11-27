package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Translates enchanting table results into curses according to the registry.
 */
public final class EnchantController implements Listener {

    private final JavaPlugin plugin;
    private final EnchantTools enchantTools;
    private final MessageConfig messages;

    public EnchantController(final JavaPlugin plugin,
                             final EnchantTools enchantTools,
                             final MessageConfig messages) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.enchantTools = Objects.requireNonNull(enchantTools, "enchantTools");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantItem(final EnchantItemEvent event) {
        final Map<Enchantment, Integer> enchantsToAdd = new HashMap<>(event.getEnchantsToAdd());
        final EnchantingInventory inventory = (EnchantingInventory) event.getInventory();
        Bukkit.getScheduler().runTask(plugin, () -> applyEnchantments(event, inventory, enchantsToAdd));
    }

    private void applyEnchantments(final EnchantItemEvent event,
                                   final EnchantingInventory inventory,
                                   final Map<Enchantment, Integer> enchantsToAdd) {
        final Map<Enchantment, Integer> translated = enchantTools.translateEnchantByChance(enchantsToAdd);
        final ItemStack item = inventory.getItem();
        if (item == null) {
            return;
        }
        final ItemStack clone = item.clone();
        enchantTools.clearEnchants(clone);
        final ItemMeta meta = clone.getItemMeta();
        if (meta != null) {
            enchantTools.addEnchants(meta, translated);
            enchantTools.updateLore(meta);
            clone.setItemMeta(meta);
        }
        inventory.setItem(clone);
        if (!translated.equals(enchantsToAdd)) {
            event.getEnchanter().sendMessage(PlaceholderText.apply(event.getEnchanter(),
                    messages.prefixed("controllers.enchant-modified", "&6Your enchantment feels different...")));
        }
    }
}
