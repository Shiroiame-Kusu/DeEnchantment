package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

/**
 * Applies curse translation to fishing loot.
 */
public final class PlayerFishController implements Listener {

    private final EnchantTools enchantTools;

    public PlayerFishController(final EnchantTools enchantTools) {
        this.enchantTools = enchantTools;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(final PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        if (!(event.getCaught() instanceof Item caught)) {
            return;
        }
        enchantTools.translateEnchantsByChance(caught.getItemStack());
    }
}
