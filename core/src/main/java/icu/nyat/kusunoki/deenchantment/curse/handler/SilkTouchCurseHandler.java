package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DeBreakBlockEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Prevents block drops when mining with the cursed tool.
 */
public final class SilkTouchCurseHandler extends AbstractCurseHandler {

    private final boolean allowContainer;

    public SilkTouchCurseHandler(final JavaPlugin plugin,
                                  final ConfigService configService,
                                  final EnchantTools enchantTools,
                                  final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.allowContainer = configBoolean(false, "allow-container", "allowContainer");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final DeBreakBlockEvent event) {
        final int level = getLevel(event);
        if (level <= 0) {
            return;
        }
        final Player player = event.getPlayer();
        if (!hasPermission(player)) {
            return;
        }
        final BlockState state = event.getDelegate().getBlock().getState();
        if (!allowContainer && state instanceof BlockInventoryHolder) {
            return;
        }
        event.getDelegate().setDropItems(false);
    }
}
