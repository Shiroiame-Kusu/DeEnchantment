package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Causes disloyal tridents to bond with nearby entities instead of returning.
 */
public final class LoyaltyCurseHandler extends AbstractCurseHandler {

    private final double chanceRate;
    private final double radius;
    private final boolean highlight;
    private final String message;

    public LoyaltyCurseHandler(final JavaPlugin plugin,
                               final ConfigService configService,
                               final EnchantTools enchantTools,
                               final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.chanceRate = Math.max(0D, Math.min(1D, configDouble(0.1D, "chance-rate", "chanceRate")));
        this.radius = Math.max(0D, configDouble(10D, "radius"));
        this.highlight = configBoolean(true, "highlight");
        this.message = configString("&c您的武器已背叛！&6现在属于 &a{player} &6位于 &a{location}", "message");
    }

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) {
            return;
        }
        final ItemStack item = trident.getItem();
        final int level = getLevel(item);
        if (level <= 0) {
            return;
        }
        if (!(trident.getShooter() instanceof LivingEntity shooter)) {
            return;
        }
        if (!hasPermission(shooter)) {
            return;
        }
        final double chance = Math.min(1.0D, level * chanceRate);
        if (chance <= 0D || ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        for (final Entity entity : trident.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living) || living.equals(shooter)) {
                continue;
            }
            if (transferTrident(shooter, living, item.clone())) {
                if (highlight) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
                }
                if (shooter instanceof Player player) {
                    final String location = living.getLocation().getBlockX() + "," +
                            living.getLocation().getBlockY() + "," + living.getLocation().getBlockZ();
                    final String rendered = color(message.replace("{player}", living.getName()).replace("{location}", location));
                    player.sendMessage(PlaceholderText.apply(player, rendered));
                }
                trident.remove();
                break;
            }
        }
    }

    private boolean transferTrident(final LivingEntity shooter,
                                    final LivingEntity target,
                                    final ItemStack tridentItem) {
        final EntityEquipment targetEquipment = target.getEquipment();
        if (targetEquipment == null) {
            return false;
        }
        final ItemStack previous = targetEquipment.getItemInMainHand();
        targetEquipment.setItemInMainHand(tridentItem);
        if (!(shooter instanceof Player)) {
            final EntityEquipment shooterEquipment = shooter.getEquipment();
            if (shooterEquipment != null) {
                shooterEquipment.setItemInMainHand(null);
            }
        }
        if (previous != null && !previous.getType().isAir()) {
            if (target instanceof Player player && player.getGameMode() != GameMode.CREATIVE) {
                final Map<Integer, ItemStack> leftovers = player.getInventory().addItem(previous.clone());
                leftovers.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
            } else {
                target.getWorld().dropItemNaturally(target.getLocation(), previous.clone());
            }
        }
        if (!(target instanceof Player) && targetEquipment != null) {
            try {
                targetEquipment.setItemInMainHandDropChance(1.0F);
            } catch (final UnsupportedOperationException ignored) {
                // some entities do not support this property
            }
        }
        return true;
    }
}
