package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Prevents cursed drops from despawning or being destroyed, keeping them bound to the owner.
 */
public final class VanishingCurseHandler extends AbstractCurseHandler {

    public VanishingCurseHandler(final JavaPlugin plugin,
                                 final ConfigService configService,
                                 final EnchantTools enchantTools,
                                 final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(final ItemDespawnEvent event) {
        final Item item = event.getEntity();
        if (!hasCurse(item.getItemStack())) {
            return;
        }
        if (!hasOwnerPermission(item.getOwner())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Item item) || item.isDead()) {
            return;
        }
        if (!hasCurse(item.getItemStack())) {
            return;
        }
        if (!hasOwnerPermission(item.getOwner())) {
            return;
        }
        event.setCancelled(true);
        final UUID throwerId = item.getThrower();
        if (throwerId == null) {
            return;
        }
        final Entity thrower = Bukkit.getEntity(throwerId);
        if (thrower == null) {
            return;
        }
        item.setFireTicks(0);
        item.teleport(thrower);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final ItemStack stack = event.getItemDrop().getItemStack();
        if (!hasCurse(stack)) {
            return;
        }
        event.getItemDrop().setOwner(event.getPlayer().getUniqueId());
    }

    private boolean hasOwnerPermission(final UUID ownerId) {
        if (ownerId == null) {
            return true;
        }
        final Player player = Bukkit.getPlayer(ownerId);
        if (player == null) {
            return true;
        }
        return hasPermission(player);
    }
}
