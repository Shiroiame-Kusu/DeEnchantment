package icu.nyat.kusunoki.deenchantment.listener.event;

import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Base event for all curse-aware interactions. Provides utilities for lazily computing
 * the cumulative level of {@link RegisteredCurse} enchantments on the relevant equipment.
 */
public abstract class DeEnchantmentEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    
    // PDC key for curses - must match EnchantTools.cursesKey
    private static final NamespacedKey CURSES_KEY = new NamespacedKey("deenchantment", "deenchantment_curses");

    private final LivingEntity entity;
    private boolean cancelled;
    private Map<RegisteredCurse, Integer> cachedLevels;

    protected DeEnchantmentEvent(final LivingEntity entity, final boolean async) {
        super(async);
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public int getCurseLevel(final RegisteredCurse curse) {
        if (curse == null) {
            return 0;
        }
        ensureCache();
        return cachedLevels.getOrDefault(curse, 0);
    }

    protected final void incrementCurse(final RegisteredCurse curse, final int level) {
        if (curse == null || level <= 0) {
            return;
        }
        ensureCache();
        cachedLevels.merge(curse, level, Integer::sum);
    }

    protected final void collectFromItem(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        // First check enchantments map (for backwards compatibility)
        item.getEnchantments().forEach((enchantment, level) -> {
            if (enchantment instanceof RegisteredCurse curse) {
                incrementCurse(curse, level);
            }
        });
        // Then check PDC for curses stored there (Paper 1.20.6+ approach)
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            final PersistentDataContainer pdc = meta.getPersistentDataContainer();
            final String data = pdc.get(CURSES_KEY, PersistentDataType.STRING);
            if (data != null && !data.isEmpty()) {
                for (String part : data.split(",")) {
                    final String[] kv = part.split(":", 2);
                    if (kv.length == 2) {
                        try {
                            final String curseKey = kv[0];
                            final int level = Integer.parseInt(kv[1]);
                            // Look up the curse by key
                            final Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(curseKey));
                            if (ench instanceof RegisteredCurse curse) {
                                incrementCurse(curse, level);
                            }
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }
        }
    }

    protected final void collectFromArray(final ItemStack[] items) {
        if (items == null) {
            return;
        }
        for (final ItemStack item : items) {
            collectFromItem(item);
        }
    }

    protected void populateCache() {
    final EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            collectFromArray(equipment.getArmorContents());
        }
    }

    private void ensureCache() {
        if (cachedLevels == null) {
            cachedLevels = new HashMap<>();
            populateCache();
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
