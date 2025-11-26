package icu.nyat.kusunoki.deenchantment.util.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;
import java.util.Set;

/**
 * Utility methods for classifying mobs without depending on modern API features.
 */
public final class EntityClassifications {

    private static final Set<EntityType> UNDEAD = EnumSet.of(
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.WITHER,
            EntityType.WITHER_SKELETON,
            EntityType.PHANTOM,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.ZOMBIE_HORSE,
            EntityType.SKELETON_HORSE,
            EntityType.ZOGLIN
    );

    private static final Set<EntityType> ARTHROPODS = EnumSet.of(
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.SILVERFISH,
            EntityType.ENDERMITE,
            EntityType.BEE
    );

    private EntityClassifications() {
    }

    public static boolean isUndead(final LivingEntity entity) {
        return entity != null && UNDEAD.contains(entity.getType());
    }

    public static boolean isArthropod(final LivingEntity entity) {
        return entity != null && ARTHROPODS.contains(entity.getType());
    }
}
