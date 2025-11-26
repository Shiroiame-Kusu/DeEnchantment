package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.listener.event.DeBreakBlockEvent;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityAttackEvent;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityHurtEvent;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityProjectileEvent;
import icu.nyat.kusunoki.deenchantment.listener.event.DeEntityShootBowEvent;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerFishEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Re-emits key Bukkit events via the custom DeEnchantment event pipeline.
 */
public final class EntityDeEnchantCaller implements Listener {

    private JavaPlugin plugin;
    private boolean registered;

    public void register(final JavaPlugin plugin) {
        if (registered) {
            return;
        }
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registered = true;
    }

    public void unregister() {
        if (!registered) {
            return;
        }
        HandlerList.unregisterAll(this);
        registered = false;
        plugin = null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityAttack(final EntityDamageByEntityEvent event) {
        final LivingEntity damager;
        if (event.getDamager() instanceof LivingEntity living) {
            damager = living;
            Bukkit.getPluginManager().callEvent(new DeEntityAttackEvent(damager, event));
            return;
        }
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity livingShooter) {
            Bukkit.getPluginManager().callEvent(new DeEntityProjectileEvent(projectile, livingShooter, event));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityHurt(final EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            Bukkit.getPluginManager().callEvent(new DeEntityHurtEvent(living, event));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {
        Bukkit.getPluginManager().callEvent(new DeBreakBlockEvent(event.getPlayer(), event));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onShootBow(final EntityShootBowEvent event) {
        if (event.getProjectile() instanceof Projectile projectile) {
            final LivingEntity living = event.getEntity();
            Bukkit.getPluginManager().callEvent(new DeEntityShootBowEvent(living, event.getBow(), projectile, event));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFish(final PlayerFishEvent event) {
        final Player player = event.getPlayer();
        Bukkit.getPluginManager().callEvent(new DePlayerFishEvent(player, event));
    }
}
