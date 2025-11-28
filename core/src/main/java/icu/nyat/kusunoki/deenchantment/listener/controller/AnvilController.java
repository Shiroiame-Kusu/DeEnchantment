package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.config.PluginConfig;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Handles interactions with anvils to merge curses correctly.
 */
public final class AnvilController implements Listener {

    private final ConfigService configService;
    private final EnchantTools enchantTools;
    private final MessageConfig messages;

    public AnvilController(final ConfigService configService,
                           final EnchantTools enchantTools,
                           final MessageConfig messages) {
        this.configService = Objects.requireNonNull(configService, "configService");
        this.enchantTools = Objects.requireNonNull(enchantTools, "enchantTools");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrepareAnvil(final PrepareAnvilEvent event) {
        final AnvilInventory inventory = event.getInventory();
        final ItemStack base = inventory.getItem(0);
        if (isEmpty(base)) {
            return;
        }
        final ItemStack addition = inventory.getItem(1);
        final String renameText = inventory.getRenameText();
        ItemStack result = event.getResult();
        if (isEmpty(result)) {
            result = null;
        }
        final String displayName = result != null && result.hasItemMeta() && result.getItemMeta().hasDisplayName()
                ? Objects.requireNonNull(result.getItemMeta().getDisplayName())
                : renameText;
        if (isEmpty(addition)) {
            if (displayName != null && !displayName.isEmpty()) {
                final ItemStack clone = base.clone();
                final ItemMeta meta = clone.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);
                    clone.setItemMeta(meta);
                    event.setResult(clone);
                }
            }
            return;
        }
        final ItemMeta baseMeta = base.getItemMeta();
        final ItemMeta additionMeta = addition.getItemMeta();
        if (baseMeta == null || additionMeta == null) {
            return;
        }
        if (base.getType() == Material.ENCHANTED_BOOK && addition.getType() != Material.ENCHANTED_BOOK) {
            return;
        }
        final Map<Enchantment, Integer> additions = additionMeta instanceof EnchantmentStorageMeta storage
                ? new HashMap<>(storage.getStoredEnchants())
                : new HashMap<>(addition.getEnchantments());
        // Also get curses from PDC of the addition item
        final Map<String, Integer> additionCurses = enchantTools.getCursesFromPdc(additionMeta);
        if (additions.isEmpty() && additionCurses.isEmpty()) {
            final ItemStack currentResult = event.getResult();
            if (isEmpty(currentResult)) {
                return;
            }
            final ItemMeta resultMeta = currentResult.getItemMeta();
            if (resultMeta == null) {
                return;
            }
            // Copy curses from base item's PDC to result item's PDC
            final Map<String, Integer> baseCurses = enchantTools.getCursesFromPdc(baseMeta);
            if (!baseCurses.isEmpty()) {
                enchantTools.copyCursesToPdc(resultMeta, baseCurses);
            }
            enchantTools.updateLore(resultMeta);
            currentResult.setItemMeta(resultMeta);
            event.setResult(currentResult);
            return;
        }
        final boolean ignoreConflicts = event.getView().getPlayer().getGameMode() == GameMode.CREATIVE;
        if (!(additionMeta instanceof EnchantmentStorageMeta) && addition.getType() != base.getType()) {
            return;
        }
        final ItemStack combined = base.clone();
        final Set<Enchantment> removals = new HashSet<>();
        final int cost = enchantTools.addEnchantments(combined, additions, removals, ignoreConflicts);
        // Also merge curses from addition item's PDC
        if (!additionCurses.isEmpty()) {
            final ItemMeta combinedMeta = combined.getItemMeta();
            if (combinedMeta != null) {
                final Map<String, Integer> baseCurses = enchantTools.getCursesFromPdc(combinedMeta);
                final Map<String, Integer> mergedCurses = new HashMap<>(baseCurses);
                for (Map.Entry<String, Integer> entry : additionCurses.entrySet()) {
                    final String key = entry.getKey();
                    final int addLevel = entry.getValue();
                    final int existingLevel = mergedCurses.getOrDefault(key, 0);
                    if (existingLevel == addLevel) {
                        mergedCurses.put(key, addLevel + 1); // Same level = upgrade
                    } else {
                        mergedCurses.put(key, Math.max(existingLevel, addLevel));
                    }
                }
                enchantTools.copyCursesToPdc(combinedMeta, mergedCurses);
                enchantTools.updateLore(combinedMeta);
                combined.setItemMeta(combinedMeta);
            }
        }
        final ItemStack existingResult = event.getResult();
        if (base.equals(existingResult)) {
            event.setResult(null);
            return;
        }
        ItemStack output = existingResult;
        if (output == null) {
            output = combined.clone();
            final ItemMeta meta = output.getItemMeta();
            if (meta instanceof Repairable repairable) {
                repairable.setRepairCost(repairable.getRepairCost() + 1);
                if (displayName != null && !displayName.isEmpty()) {
                    repairable.setDisplayName(displayName);
                }
                output.setItemMeta((ItemMeta) repairable);
            }
        } else {
            final ItemMeta meta = output.getItemMeta();
            if (meta != null) {
                removals.forEach(meta::removeEnchant);
                final ItemStack finalOutput = output;
                // Copy vanilla enchantments
                combined.getEnchantments().forEach((enchantment, level) -> {
                    if (meta instanceof EnchantmentStorageMeta storage) {
                        storage.addStoredEnchant(enchantment, level, true);
                        finalOutput.setItemMeta(storage);
                    } else {
                        meta.addEnchant(enchantment, level, true);
                        finalOutput.setItemMeta(meta);
                    }
                });
                // Copy curses from PDC
                final ItemMeta combinedMeta = combined.getItemMeta();
                if (combinedMeta != null) {
                    final Map<String, Integer> curses = enchantTools.getCursesFromPdc(combinedMeta);
                    if (!curses.isEmpty()) {
                        enchantTools.copyCursesToPdc(meta, curses);
                    }
                }
                enchantTools.updateLore(meta);
                if (displayName != null && !displayName.isEmpty()) {
                    meta.setDisplayName(displayName);
                    output.setItemMeta(meta);
                }
            }
        }
        if (!(baseMeta instanceof EnchantmentStorageMeta) && output.getEnchantments().equals(base.getEnchantments())) {
            event.setResult(null);
            return;
        }
        inventory.setRepairCost(inventory.getRepairCost() + cost);
        if (inventory.getRepairCost() >= 40) {
            final PluginConfig config = configService.plugin();
            if (!config.isIgnoreTooExpensive() && !ignoreConflicts) {
                event.setResult(null);
                return;
            }
            if (!inventory.getViewers().isEmpty()) {
                inventory.getViewers().get(0).sendMessage(
                        PlaceholderText.apply(inventory.getViewers().get(0),
                                messages.prefixed("controllers.anvil-cost", "&aThis enchantment costs {0}", inventory.getRepairCost()))
                );
            }
        } else {
            event.setResult(output);
        }
    }

    private boolean isEmpty(final ItemStack item) {
        return item == null || item.getType().isAir();
    }
}
