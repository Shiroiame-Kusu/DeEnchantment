package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

/**
 * Injects curses into villager trades.
 */
public final class MerchantController implements Listener {

    private final EnchantTools enchantTools;

    public MerchantController(final EnchantTools enchantTools) {
        this.enchantTools = enchantTools;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVillagerAcquireTrade(final VillagerAcquireTradeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final MerchantRecipe recipe = event.getRecipe();
        final ItemStack result = recipe.getResult();
        if (enchantTools.getEnchantOrStoredEnchant(result).isEmpty()) {
            return;
        }
        final ItemStack mutated = result.clone();
        enchantTools.translateEnchantsByChance(mutated);
        final MerchantRecipe replacement = new MerchantRecipe(mutated, recipe.getMaxUses());
        replacement.setUses(recipe.getUses());
        replacement.setPriceMultiplier(recipe.getPriceMultiplier());
        replacement.setIngredients(recipe.getIngredients());
        replacement.setExperienceReward(recipe.hasExperienceReward());
        replacement.setVillagerExperience(recipe.getVillagerExperience());
        event.setRecipe(replacement);
    }
}
