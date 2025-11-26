package icu.nyat.kusunoki.deenchantment.listener.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;

/**
 * Fired when a projectile launched by a cursed entity damages another entity.
 */
public final class DeEntityProjectileEvent extends DeEnchantmentEvent {

    private final Projectile projectile;
    private final EntityDamageByEntityEvent delegate;

    public DeEntityProjectileEvent(final Projectile projectile,
                                   final LivingEntity attacker,
                                   final EntityDamageByEntityEvent delegate) {
        super(attacker, false);
        this.projectile = projectile;
        this.delegate = delegate;
    }

    public Projectile getProjectile() {
        return projectile;
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
