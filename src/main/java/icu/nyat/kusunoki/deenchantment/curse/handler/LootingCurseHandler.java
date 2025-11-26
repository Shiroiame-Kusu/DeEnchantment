package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Removes loot drops from mobs slain with the cursed weapon.
 */
public final class LootingCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;

    public LootingCurseHandler(final JavaPlugin plugin,
                                final ConfigService configService,
                                final EnchantTools enchantTools,
                                final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, Math.min(1D, configDouble(0.05D, "chance-rate", "chanceRate")));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (event.getDrops().isEmpty()) {
            return;
        }
        final LivingEntity killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        final int level = getLevelFromMainHand(killer);
        if (level <= 0) {
            return;
        }
        if (!hasPermission(killer)) {
            return;
        }
        final double chance = Math.min(1.0D, level * chanceRate);
        if (chance <= 0D || ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }
        event.getDrops().clear();
    }

    private int getLevelFromMainHand(final LivingEntity entity) {
        final EntityEquipment equipment = entity.getEquipment();
        final ItemStack mainHand = equipment == null ? null : equipment.getItemInMainHand();
        return getLevel(mainHand);
    }
}
