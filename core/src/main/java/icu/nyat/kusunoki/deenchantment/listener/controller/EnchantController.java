package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.curse.CurseRegistry;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Translates enchanting table results into curses according to the registry.
 * Curses appear based on their configured chance - they don't replace all enchantments.
 */
public final class EnchantController implements Listener {

    private final JavaPlugin plugin;
    private final EnchantTools enchantTools;
    private final MessageConfig messages;
    private final CurseRegistry curseRegistry;

    public EnchantController(final JavaPlugin plugin,
                             final EnchantTools enchantTools,
                             final MessageConfig messages,
                             final CurseRegistry curseRegistry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.enchantTools = Objects.requireNonNull(enchantTools, "enchantTools");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.curseRegistry = Objects.requireNonNull(curseRegistry, "curseRegistry");
    }

    /**
     * Prevents items that already have curses from being enchanted again.
     * Since curses are stored in PDC, the enchanting table doesn't know about them.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareEnchant(final PrepareItemEnchantEvent event) {
        final ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        // Check if item already has curses in PDC
        final Map<String, Integer> curses = enchantTools.getCursesFromPdc(item);
        if (!curses.isEmpty()) {
            // Item already has curses, prevent enchanting
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantItem(final EnchantItemEvent event) {
        final Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        
        // Translate some enchantments to curses based on chance
        final Map<RegisteredCurse, Integer> cursesToAdd = new HashMap<>();
        final java.util.Iterator<Map.Entry<Enchantment, Integer>> iterator = enchantsToAdd.entrySet().iterator();
        
        while (iterator.hasNext()) {
            final Map.Entry<Enchantment, Integer> entry = iterator.next();
            final Enchantment source = entry.getKey();
            final int level = entry.getValue();
            
            // Try to find corresponding curse
            final String candidateKey = "de_" + source.getKey().getKey().toLowerCase(Locale.ROOT);
            final Optional<RegisteredCurse> curseOpt = curseRegistry.find(candidateKey);
            
            if (curseOpt.isPresent()) {
                final RegisteredCurse curse = curseOpt.get();
                final double chance = Math.max(0.0D, Math.min(1.0D, curse.definition().chance()));
                // Roll the dice - if successful, replace with curse
                if (ThreadLocalRandom.current().nextDouble() < chance) {
                    // Remove the original enchantment and add curse instead
                    iterator.remove();
                    cursesToAdd.put(curse, level);
                }
            }
        }
        
        // If any curses were added, apply them after the event completes
        if (!cursesToAdd.isEmpty()) {
            final ItemStack item = event.getItem();
            Bukkit.getScheduler().runTask(plugin, () -> {
                final ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    // Add curses to PDC
                    for (Map.Entry<RegisteredCurse, Integer> entry : cursesToAdd.entrySet()) {
                        enchantTools.addCurseToPdc(meta, entry.getKey(), entry.getValue());
                    }
                    enchantTools.updateLore(meta);
                    item.setItemMeta(meta);
                }
                event.getEnchanter().sendMessage(PlaceholderText.apply(event.getEnchanter(),
                        messages.prefixed("controllers.enchant-modified", "&6你的附魔似乎有些不对劲...")));
            });
        }
    }
}
