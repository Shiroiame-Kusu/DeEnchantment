package icu.nyat.kusunoki.deenchantment.listener.event;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EntityEquipment;

/**
 * Fired when a player with cursed fishing gear interacts with the fishing event system.
 */
public final class DePlayerFishEvent extends DeEnchantmentEvent {

    private final Player player;
    private final PlayerFishEvent delegate;

    public DePlayerFishEvent(final Player player, final PlayerFishEvent delegate) {
        super(player, false);
        this.player = player;
        this.delegate = delegate;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerFishEvent getDelegate() {
        return delegate;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        super.setCancelled(cancel);
        delegate.setCancelled(cancel);
    }

    @Override
    protected void populateCache() {
    final EntityEquipment equipment = player.getEquipment();
        if (equipment != null) {
            collectFromItem(equipment.getItemInMainHand());
            collectFromItem(equipment.getItemInOffHand());
        }
    }
}
