package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Trades extra durability loss for experience whenever cursed items take damage.
 */
public final class MendingCurseHandler extends AbstractCurseHandler {

    private final double expRate;
    private final double damageRate;

    public MendingCurseHandler(final JavaPlugin plugin,
                               final ConfigService configService,
                               final EnchantTools enchantTools,
                               final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.expRate = Math.max(0D, configDouble(1.0D, "exp-rate", "expRate"));
        this.damageRate = Math.max(0D, configDouble(1.0D, "damage-rate", "damageRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(final PlayerItemDamageEvent event) {
        final ItemStack item = event.getItem();
        final int level = getLevel(item);
        if (level <= 0) {
            return;
        }
        if (event.getDamage() < 1) {
            return;
        }
        final Player player = event.getPlayer();
        if (!hasPermission(player)) {
            return;
        }
        final int extraDamage = (int) Math.round(level * damageRate);
        if (extraDamage > 0) {
            event.setDamage(event.getDamage() + extraDamage);
        }
        final int experience = (int) Math.round(level * expRate);
        if (experience > 0) {
            player.giveExp(experience);
        }
    }
}
