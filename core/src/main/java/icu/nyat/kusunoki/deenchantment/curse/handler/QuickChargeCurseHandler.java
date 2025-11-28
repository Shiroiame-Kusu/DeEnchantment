package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies slowness when charging a crossbow, making the charging process more vulnerable.
 */
public final class QuickChargeCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;
    private final int slownessDuration;
    private final int slownessLevelRate;

    public QuickChargeCurseHandler(final JavaPlugin plugin,
                                   final ConfigService configService,
                                   final EnchantTools enchantTools,
                                   final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, configDouble(0.3D, "chance-rate", "chanceRate"));
        this.slownessDuration = Math.max(20, configInt(60, "slowness-duration", "slownessDuration"));
        this.slownessLevelRate = Math.max(1, configInt(1, "slowness-level-rate", "slownessLevelRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        final ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.CROSSBOW || !hasCurse(item)) {
            return;
        }
        // Check if crossbow is not yet loaded
        if (!(item.getItemMeta() instanceof CrossbowMeta meta)) {
            return;
        }
        if (meta.hasChargedProjectiles()) {
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
        final double chance = Math.min(1.0D, level * chanceRate);
        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }
        // Apply slowness while charging
        final int amplifier = Math.max(0, (level * slownessLevelRate) - 1);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW,
                slownessDuration,
                amplifier,
                true,
                false,
                true
        ));
    }
}
