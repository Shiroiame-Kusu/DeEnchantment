package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * Propels burning players forward instead of throwing their tridents.
 */
public final class RiptideCurseHandler extends AbstractCurseHandler {

    private final double speedRate;

    public RiptideCurseHandler(final JavaPlugin plugin,
                               final ConfigService configService,
                               final EnchantTools enchantTools,
                               final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.speedRate = Math.max(0D, configDouble(1.0D, "speed-rate", "speedRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) {
            return;
        }
        if (!(trident.getShooter() instanceof Player player)) {
            return;
        }
        final int level = getLevel(trident.getItem());
        if (level <= 0) {
            return;
        }
        if (!hasPermission(player)) {
            return;
        }
        event.setCancelled(true);
        if (player.getFireTicks() <= 0) {
            return;
        }
        final Vector direction = player.getLocation().getDirection();
        if (direction.lengthSquared() == 0D) {
            return;
        }
        final double multiplier = Math.max(0D, level * speedRate);
        player.setVelocity(direction.normalize().multiply(multiplier));
        damageHeldTrident(player, trident.getItem());
    }

    private void damageHeldTrident(final Player player, final ItemStack reference) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (damageStack(player, EquipmentSlot.HAND, reference)) {
            return;
        }
        damageStack(player, EquipmentSlot.OFF_HAND, reference);
    }

    private boolean damageStack(final Player player,
                                final EquipmentSlot slot,
                                final ItemStack reference) {
        final ItemStack stack = player.getInventory().getItem(slot);
        if (!isSameTrident(stack, reference)) {
            return false;
        }
        final ItemMeta meta = stack.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return false;
        }
        final PlayerItemDamageEvent damageEvent = new PlayerItemDamageEvent(player, stack, 1);
        plugin.getServer().getPluginManager().callEvent(damageEvent);
        if (damageEvent.isCancelled()) {
            return true;
        }
        damageable.setDamage(damageable.getDamage() + damageEvent.getDamage());
        stack.setItemMeta(meta);
        if (damageable.getDamage() >= stack.getType().getMaxDurability()) {
            player.getInventory().setItem(slot, null);
            player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
            final PlayerItemBreakEvent breakEvent = new PlayerItemBreakEvent(player, stack);
            plugin.getServer().getPluginManager().callEvent(breakEvent);
        }
        return true;
    }

    private boolean isSameTrident(final ItemStack stack, final ItemStack reference) {
        if (stack == null || reference == null) {
            return false;
        }
        if (stack == reference) {
            return true;
        }
        if (stack.getType() != reference.getType()) {
            return false;
        }
        return stack.isSimilar(reference);
    }
}
