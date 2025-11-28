package icu.nyat.kusunoki.deenchantment.curse;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class RegisteredCurse extends Enchantment {

    private final NamespacedKey key;
    private volatile CurseDefinition definition;

    public RegisteredCurse(final CurseDefinition definition) {
        super();
        this.key = definition.id().namespacedKey();
        this.definition = definition;
    }

    public CurseDefinition definition() {
        return definition;
    }

    public void refreshDefinition(final CurseDefinition updated) {
        if (!definition.id().equals(updated.id())) {
            throw new IllegalArgumentException("Curse definition ID mismatch for " + getKey());
        }
        this.definition = updated;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public String getName() {
        return definition.id().name();
    }

    @Override
    public int getMaxLevel() {
        return definition.maxLevel();
    }

    @Override
    public int getStartLevel() {
        return definition.startLevel();
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return definition.target();
    }

    @Override
    public boolean isTreasure() {
        return definition.treasure();
    }

    @Override
    public boolean isCursed() {
        return definition.cursed();
    }

    @Override
    public boolean isTradeable() {
        return !definition.cursed();
    }

    @Override
    public boolean isDiscoverable() {
        return !definition.cursed();
    }

    @Override
    public boolean conflictsWith(final Enchantment other) {
        final Set<String> conflicts = definition.conflicts();
        final String key = other.getKey().getKey().toLowerCase(Locale.ROOT);
        return conflicts.contains(key);
    }

    @Override
    public boolean canEnchantItem(final ItemStack item) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            return true;
        }
        return definition.target().includes(item);
    }

    @Override
    public Set<EquipmentSlot> getActiveSlots() {
        return EnumSet.allOf(EquipmentSlot.class);
    }

    @Override
    public float getDamageIncrease(final int level, final EntityCategory entityCategory) {
        return 0.0F;
    }

    @Override
    public EnchantmentRarity getRarity() {
        return definition.treasure() ? EnchantmentRarity.VERY_RARE : EnchantmentRarity.RARE;
    }

    // No @Override here: the method only exists on Paper 1.21+, but defining it keeps runtime compatibility.
    public int getWeight() {
        // Weight corresponds to rarity: COMMON=10, UNCOMMON=5, RARE=2, VERY_RARE=1
        return definition.treasure() ? 1 : 2;
    }

    // No @Override here: the method signature varies between versions.
    public int getMinModifiedCost(final int level) {
        return Math.max(1, level * 5);
    }

    // No @Override here: the method signature varies between versions.
    public int getMaxModifiedCost(final int level) {
        return getMinModifiedCost(level) + 10;
    }

    // No @Override here: the method only exists on Paper 1.21+, but defining it keeps runtime compatibility.
    public int getAnvilCost() {
        return 0;
    }

    @Override
    public Component displayName(final int level) {
        return LegacyComponentSerializer.legacySection().deserialize(definition.displayName());
    }

    // No @Override here: the method signature varies between versions.
    public String translationKey() {
        return "enchantment." + definition.id().key();
    }

    // No @Override here: deprecated in some versions.
    public String getTranslationKey() {
        return translationKey();
    }

    public Optional<Enchantment> vanillaEnchantment() {
        return definition.id().vanillaEnchantment();
    }
}
