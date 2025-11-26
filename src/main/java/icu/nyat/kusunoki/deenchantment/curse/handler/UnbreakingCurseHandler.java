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
 * Increases durability consumption on cursed tools/armor.
 */
public final class UnbreakingCurseHandler extends AbstractCurseHandler {

    private final double damageRate;

    public UnbreakingCurseHandler(final JavaPlugin plugin,
                                   final ConfigService configService,
                                   final EnchantTools enchantTools,
                                   final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.damageRate = Math.max(0D, configDouble(1D, "damage-rate", "damageRate"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(final PlayerItemDamageEvent event) {
        final ItemStack item = event.getItem();
        final int level = getLevel(item);
        if (level <= 0) {
            return;
        }
        final Player player = event.getPlayer();
        if (!hasPermission(player)) {
            return;
        }
        final int extra = (int) Math.round(level * damageRate);
        if (extra <= 0) {
            return;
        }
        event.setDamage(Math.max(0, event.getDamage() + extra));
    }
}
