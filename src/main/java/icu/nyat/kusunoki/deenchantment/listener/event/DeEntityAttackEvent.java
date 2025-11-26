package icu.nyat.kusunoki.deenchantment.listener.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;

/**
 * Fired when a living entity with a cursed weapon damages another entity.
 */
public final class DeEntityAttackEvent extends DeEnchantmentEvent {

    private final EntityDamageByEntityEvent delegate;

    public DeEntityAttackEvent(final LivingEntity attacker, final EntityDamageByEntityEvent delegate) {
        super(attacker, false);
        this.delegate = delegate;
    }

    public EntityDamageByEntityEvent getDelegate() {
        return delegate;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        super.setCancelled(cancel);
        delegate.setCancelled(cancel);
    }

    @Override
    protected void populateCache() {
    final EntityEquipment equipment = getEntity().getEquipment();
        if (equipment != null) {
            collectFromItem(equipment.getItemInMainHand());
        }
    }
}
