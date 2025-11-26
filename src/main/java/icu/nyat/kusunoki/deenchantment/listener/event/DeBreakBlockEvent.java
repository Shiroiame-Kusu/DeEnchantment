package icu.nyat.kusunoki.deenchantment.listener.event;

import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EntityEquipment;

/**
 * Fired when a player with cursed tools breaks a block.
 */
public final class DeBreakBlockEvent extends DeEnchantmentEvent {

    private final Player player;
    private final BlockBreakEvent delegate;

    public DeBreakBlockEvent(final Player player, final BlockBreakEvent delegate) {
        super(player, false);
        this.player = player;
        this.delegate = delegate;
    }

    public Player getPlayer() {
        return player;
    }

    public BlockBreakEvent getDelegate() {
        return delegate;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        super.setCancelled(cancel);
        delegate.setCancelled(cancel);
    }

    @Override
    public int getCurseLevel(final RegisteredCurse curse) {
        return super.getCurseLevel(curse);
    }

    @Override
    protected void populateCache() {
    final EntityEquipment equipment = player.getEquipment();
        if (equipment != null) {
            collectFromItem(equipment.getItemInMainHand());
        }
    }
}
