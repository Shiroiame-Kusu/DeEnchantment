package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Repeatedly interrupts crossbow charging attempts before finally allowing it.
 */
public final class QuickChargeCurseHandler extends AbstractCurseHandler {

    private final int timeRate;
    private final Map<ItemStack, Integer> remainingInterrupts = new HashMap<>();

    public QuickChargeCurseHandler(final JavaPlugin plugin,
                                   final ConfigService configService,
                                   final EnchantTools enchantTools,
                                   final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.timeRate = Math.max(0, configInt(1, "time-rate", "timeRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        final ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.CROSSBOW || !hasCurse(item)) {
            return;
        }
        final CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
        if (meta == null || meta.hasChargedProjectiles()) {
            return;
        }
        final int level = getLevel(item);
        if (level <= 0) {
            return;
        }
        final Player player = event.getPlayer();
        if (!hasPermission(player)) {
            return;
        }
        final EquipmentSlot hand = event.getHand();
        if (hand == null) {
            return;
        }
        final EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return;
        }
        final int allowance = Math.max(0, level * timeRate);
        if (allowance <= 0) {
            return;
        }
        final int remaining = remainingInterrupts.getOrDefault(item, allowance);
        if (remaining <= 0) {
            remainingInterrupts.remove(item);
            return;
        }
        remainingInterrupts.put(item, remaining - 1);
        equipment.setItem(hand, null);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || player.isDead()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                return;
            }
            final EntityEquipment currentEquipment = player.getEquipment();
            if (currentEquipment == null) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                return;
            }
            final ItemStack existing = currentEquipment.getItem(hand);
            if (existing == null || existing.getType().isAir()) {
                currentEquipment.setItem(hand, item);
            } else {
                player.getInventory().addItem(item);
            }
        }, 2L);
    }

    @Override
    public void disable() {
        remainingInterrupts.clear();
    }
}
