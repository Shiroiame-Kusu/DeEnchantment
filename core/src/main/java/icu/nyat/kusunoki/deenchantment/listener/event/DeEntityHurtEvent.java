package icu.nyat.kusunoki.deenchantment.listener.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Fired when a living entity wearing cursed equipment takes damage.
 */
public final class DeEntityHurtEvent extends DeEnchantmentEvent {

    private final EntityDamageEvent delegate;

    public DeEntityHurtEvent(final LivingEntity entity, final EntityDamageEvent delegate) {
        super(entity, false);
        this.delegate = delegate;
    }

    public EntityDamageEvent getDelegate() {
        return delegate;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        super.setCancelled(cancel);
        delegate.setCancelled(cancel);
    }
}
