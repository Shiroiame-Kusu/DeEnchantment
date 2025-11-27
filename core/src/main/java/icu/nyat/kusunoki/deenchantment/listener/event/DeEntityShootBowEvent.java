package icu.nyat.kusunoki.deenchantment.listener.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Fired when a cursed bow or crossbow is used.
 */
public final class DeEntityShootBowEvent extends DeEnchantmentEvent {

    private final ItemStack bow;
    private final Projectile projectile;
    private final EntityShootBowEvent delegate;

    public DeEntityShootBowEvent(final LivingEntity shooter,
                                 final ItemStack bow,
                                 final Projectile projectile,
                                 final EntityShootBowEvent delegate) {
        super(shooter, false);
        this.bow = bow;
        this.projectile = projectile;
        this.delegate = delegate;
    }

    public ItemStack getBow() {
        return bow;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public EntityShootBowEvent getDelegate() {
        return delegate;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        super.setCancelled(cancel);
        delegate.setCancelled(cancel);
    }

    @Override
    protected void populateCache() {
        collectFromItem(bow);
    }
}
