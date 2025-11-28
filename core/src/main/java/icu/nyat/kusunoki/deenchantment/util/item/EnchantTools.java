package icu.nyat.kusunoki.deenchantment.util.item;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.config.PluginConfig;
import icu.nyat.kusunoki.deenchantment.curse.CurseRegistry;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility helpers for mutating item enchantments and maintaining lore metadata.
 */
public final class EnchantTools {

    private static final LegacyRomanNumeral ROMAN = new LegacyRomanNumeral();

    private final NamespacedKey nameKey;
    private final NamespacedKey descriptionKey;
    private final NamespacedKey cursesKey;
    private final ConfigService configService;
    private final CurseRegistry curseRegistry;

    public EnchantTools(final JavaPlugin plugin,
                        final ConfigService configService,
                        final CurseRegistry curseRegistry) {
        this.nameKey = new NamespacedKey(Objects.requireNonNull(plugin, "plugin"), "deenchantment_name");
        this.descriptionKey = new NamespacedKey(plugin, "deenchantment_description");
        this.cursesKey = new NamespacedKey(plugin, "deenchantment_curses");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.curseRegistry = Objects.requireNonNull(curseRegistry, "curseRegistry");
    }

    public int addEnchantments(final ItemStack target,
                               final Map<Enchantment, Integer> additions,
                               final Set<Enchantment> removes,
                               final boolean ignoreConflicts) {
        if (target == null || additions == null || additions.isEmpty()) {
            return 0;
        }
        final ItemMeta meta = target.getItemMeta();
        if (meta == null) {
            return 0;
        }
        final Map<Enchantment, Integer> enchantments = meta instanceof EnchantmentStorageMeta storage
                ? new HashMap<>(storage.getStoredEnchants())
                : new HashMap<>(meta.getEnchants());
        if (enchantments.isEmpty() && additions.isEmpty()) {
            return 0;
        }
        int cost = 0;
        final PluginConfig config = configService.plugin();
        for (Map.Entry<Enchantment, Integer> entry : additions.entrySet()) {
            final Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            if (!ignoreConflicts) {
                if (enchantments.containsKey(enchant)) {
                    // Duplicate entry will be merged below
                } else if (!enchant.canEnchantItem(target)) {
                    continue;
                }
                boolean conflicted = false;
                for (Enchantment existing : enchantments.keySet()) {
                    if (existing.equals(enchant)) {
                        continue;
                    }
                    if (existing.conflictsWith(enchant) || enchant.conflictsWith(existing)) {
                        conflicted = true;
                        if (removes != null) {
                            removes.add(enchant);
                        }
                        break;
                    }
                }
                if (conflicted) {
                    continue;
                }
            }
            if (enchantments.containsKey(enchant)) {
                final int current = enchantments.get(enchant);
                if (current > level) {
                    level = current;
                } else if (current == level) {
                    level++;
                }
            }
            if (!ignoreConflicts && !config.isAllowLevelUnlimited() && level > enchant.getMaxLevel()) {
                level = enchant.getMaxLevel();
            }
            enchantments.put(enchant, level);
            if (enchant instanceof RegisteredCurse) {
                cost += level;
            }
        }
        apply(meta, enchantments);
        target.setItemMeta(meta);
        return cost;
    }

    public void clearEnchantLore(final ItemMeta meta) {
        if (meta == null || !meta.hasLore()) {
            return;
        }
    final List<String> sourceLore = meta.getLore() == null ? Collections.emptyList() : meta.getLore();
    final List<String> lore = new ArrayList<>(sourceLore);
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        final PersistentDataContainer names = container.get(nameKey, PersistentDataType.TAG_CONTAINER);
        final PersistentDataContainer descriptions = container.get(descriptionKey, PersistentDataType.TAG_CONTAINER);
        if (names != null) {
            for (NamespacedKey key : names.getKeys()) {
                final String value = names.get(key, PersistentDataType.STRING);
                if (value != null) {
                    lore.remove(value);
                }
            }
            container.remove(nameKey);
        }
        if (descriptions != null) {
            for (NamespacedKey key : descriptions.getKeys()) {
                final String value = descriptions.get(key, PersistentDataType.STRING);
                if (value != null) {
                    lore.remove(value);
                }
            }
            container.remove(descriptionKey);
        }
        meta.setLore(lore);
    }

    public void translateEnchantsByChance(final ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        try {
            final Map<Enchantment, Integer> present = new HashMap<>(getEnchantOrStoredEnchant(itemStack));
            if (present.isEmpty()) {
                return;
            }
            final Map<Enchantment, Integer> translated = translateEnchantByChance(present);
            final ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) {
                return;
            }
            clearEnchants(itemStack);
            apply(meta, translated);
            itemStack.setItemMeta(meta);
        } catch (final Throwable ignored) {
            // Swallow unexpected Paper/PDC quirks to avoid crashing the entire command.
        }
    }

    public void clearEnchants(final ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        final ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        if (meta instanceof EnchantmentStorageMeta storage) {
            for (Enchantment enchantment : List.copyOf(storage.getStoredEnchants().keySet())) {
                storage.removeStoredEnchant(enchantment);
            }
            itemStack.setItemMeta(storage);
        } else {
            for (Enchantment enchantment : List.copyOf(meta.getEnchants().keySet())) {
                meta.removeEnchant(enchantment);
            }
            itemStack.setItemMeta(meta);
        }
    }

    public Map<Enchantment, Integer> getEnchantOrStoredEnchant(final ItemStack itemStack) {
        final ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return Collections.emptyMap();
        }
        if (meta instanceof EnchantmentStorageMeta storage) {
            return storage.getStoredEnchants();
        }
        return meta.getEnchants();
    }

    public Map<Enchantment, Integer> translateEnchantByChance(final Map<Enchantment, Integer> enchantments) {
        final Map<Enchantment, Integer> mutable = new HashMap<>(enchantments);
        final Map<Enchantment, Integer> removals = new LinkedHashMap<>();
        final Map<RegisteredCurse, Integer> additions = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            final Enchantment source = entry.getKey();
            final String candidateKey = "de_" + source.getKey().getKey().toLowerCase(Locale.ROOT);
            final Optional<RegisteredCurse> target = curseRegistry.find(candidateKey);
            if (target.isEmpty()) {
                continue;
            }
            final RegisteredCurse curse = target.get();
            final double chance = Math.max(0.0D, Math.min(1.0D, curse.definition().chance()));
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                continue;
            }
            removals.put(source, entry.getValue());
            additions.put(curse, entry.getValue());
        }
        final Iterator<Map.Entry<RegisteredCurse, Integer>> iterator = additions.entrySet().iterator();
        for (Map.Entry<Enchantment, Integer> removal : removals.entrySet()) {
            mutable.remove(removal.getKey());
            if (iterator.hasNext()) {
                final Map.Entry<RegisteredCurse, Integer> next = iterator.next();
                mutable.put(next.getKey(), next.getValue());
            }
        }
        return mutable;
    }

    public void updateLore(final ItemMeta meta) {
        if (meta == null) {
            return;
        }
        if (meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            return;
        }
        clearEnchantLore(meta);
        
        // Read curses from PDC instead of enchantment map
        final Map<String, Integer> cursesData = getCursesFromPdc(meta);
        if (cursesData.isEmpty()) {
            return;
        }
        
        final PluginConfig config = configService.plugin();
        final List<String> loreEntries = new ArrayList<>();
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        final PersistentDataContainer names = container.getAdapterContext().newPersistentDataContainer();
        final PersistentDataContainer descriptions = container.getAdapterContext().newPersistentDataContainer();
        boolean hasDescriptions = false;
        
        for (Map.Entry<String, Integer> entry : cursesData.entrySet()) {
            final String curseKeyStr = entry.getKey();
            final int level = entry.getValue();
            
            // Look up the curse by its key
            final Optional<RegisteredCurse> curseOpt = curseRegistry.find(curseKeyStr);
            if (curseOpt.isEmpty()) {
                continue;
            }
            final RegisteredCurse curse = curseOpt.get();
            
            final String label = resolveLabel(curse, level);
            loreEntries.add(label);
            names.set(curse.getKey(), PersistentDataType.STRING, label);
            if (config.isShowDescription()) {
                final String description = curse.definition().description();
                loreEntries.add(description);
                descriptions.set(curse.getKey(), PersistentDataType.STRING, description);
                hasDescriptions = true;
            }
        }
        if (loreEntries.isEmpty()) {
            return;
        }
        container.set(nameKey, PersistentDataType.TAG_CONTAINER, names);
        if (hasDescriptions) {
            container.set(descriptionKey, PersistentDataType.TAG_CONTAINER, descriptions);
        }
        if (!meta.hasLore()) {
            meta.setLore(loreEntries);
            return;
        }
    final List<String> baseLore = meta.getLore() == null ? Collections.emptyList() : meta.getLore();
    final List<String> lore = new ArrayList<>(baseLore);
        final int position = Math.max(0, Math.min(config.getLorePosition(), lore.size()));
        lore.addAll(position, loreEntries);
        meta.setLore(lore);
    }

    public void addEnchants(final ItemMeta meta, final Map<Enchantment, Integer> enchants) {
        if (meta == null || enchants == null || enchants.isEmpty()) {
            return;
        }
        // Separate vanilla enchantments from custom curses
        final Map<Enchantment, Integer> vanillaEnchants = new HashMap<>();
        final Map<RegisteredCurse, Integer> customCurses = new HashMap<>();
        
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (entry.getKey() instanceof RegisteredCurse curse) {
                customCurses.put(curse, entry.getValue());
            } else {
                vanillaEnchants.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Add vanilla enchantments normally via Bukkit API
        if (meta instanceof EnchantmentStorageMeta storage) {
            for (Map.Entry<Enchantment, Integer> entry : vanillaEnchants.entrySet()) {
                storage.addStoredEnchant(entry.getKey(), entry.getValue(), true);
            }
        } else {
            for (Map.Entry<Enchantment, Integer> entry : vanillaEnchants.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }
        
        // Store custom curses in PDC instead of as real enchantments
        // This avoids Paper 1.20.6+'s Handleable check during setItemMeta
        if (!customCurses.isEmpty()) {
            storeCursesInPdc(meta, customCurses);
        }
    }
    
    /**
     * Stores curses in PersistentDataContainer to avoid Paper's Handleable serialization issues.
     * Format: "curse_key:level,curse_key2:level2,..." (e.g., "de_protection:1,de_sharpness:2")
     */
    private void storeCursesInPdc(final ItemMeta meta, final Map<RegisteredCurse, Integer> curses) {
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<RegisteredCurse, Integer> entry : curses.entrySet()) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            // Store just the key part (e.g., "de_protection"), not the full namespaced key
            builder.append(entry.getKey().getKey().getKey())
                   .append(":")
                   .append(entry.getValue());
        }
        container.set(cursesKey, PersistentDataType.STRING, builder.toString());
    }
    
    /**
     * Retrieves curses stored in PDC for an item.
     * @return Map of curse keys (e.g., "de_protection") to levels
     */
    public Map<String, Integer> getCursesFromPdc(final ItemMeta meta) {
        if (meta == null) {
            return Collections.emptyMap();
        }
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        final String data = container.get(cursesKey, PersistentDataType.STRING);
        if (data == null || data.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, Integer> result = new HashMap<>();
        for (String part : data.split(",")) {
            final String[] kv = part.split(":", 2);
            if (kv.length == 2) {
                try {
                    result.put(kv[0], Integer.parseInt(kv[1]));
                } catch (NumberFormatException ignored) {}
            }
        }
        return result;
    }
    
    /**
     * Retrieves curses from an ItemStack's PDC.
     */
    public Map<String, Integer> getCursesFromPdc(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Collections.emptyMap();
        }
        return getCursesFromPdc(item.getItemMeta());
    }
    
    /**
     * Gets the total level of a specific curse on an item.
     */
    public int getCurseLevel(final ItemStack item, final RegisteredCurse curse) {
        final Map<String, Integer> curses = getCursesFromPdc(item);
        return curses.getOrDefault(curse.getKey().getKey(), 0);
    }
    
    /**
     * Copies curses from a map to an item's PDC.
     * Used for anvil operations to transfer curses between items.
     */
    public void copyCursesToPdc(final ItemMeta meta, final Map<String, Integer> curses) {
        if (meta == null || curses == null || curses.isEmpty()) {
            return;
        }
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : curses.entrySet()) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(entry.getKey())
                   .append(":")
                   .append(entry.getValue());
        }
        container.set(cursesKey, PersistentDataType.STRING, builder.toString());
    }
    
    /**
     * Adds a single curse to an item's PDC, merging with existing curses.
     */
    public void addCurseToPdc(final ItemMeta meta, final RegisteredCurse curse, final int level) {
        if (meta == null || curse == null || level <= 0) {
            return;
        }
        final Map<String, Integer> existing = getCursesFromPdc(meta);
        final Map<String, Integer> merged = new HashMap<>(existing);
        final String key = curse.getKey().getKey();
        // Merge levels if curse already exists
        final int existingLevel = merged.getOrDefault(key, 0);
        if (existingLevel == level) {
            merged.put(key, level + 1); // Same level = upgrade
        } else {
            merged.put(key, Math.max(existingLevel, level));
        }
        copyCursesToPdc(meta, merged);
    }

    public int getLevelCount(final LivingEntity entity, final Enchantment enchantment) {
        if (entity == null || enchantment == null) {
            return 0;
        }
        if (entity.getEquipment() == null) {
            return 0;
        }
        int total = 0;
        for (ItemStack stack : entity.getEquipment().getArmorContents()) {
            if (stack == null) {
                continue;
            }
            // For curses, use PDC-based lookup; for vanilla enchantments, use the enchantment map
            if (enchantment instanceof RegisteredCurse curse) {
                total += getCurseLevel(stack, curse);
            } else {
                final Integer level = stack.getEnchantments().get(enchantment);
                if (level != null) {
                    total += level;
                }
            }
        }
        return total;
    }

    private void apply(final ItemMeta meta, final Map<Enchantment, Integer> enchantments) {
        clearEnchantLore(meta);
        addEnchants(meta, enchantments);
        updateLore(meta);
    }

    private String resolveLabel(final RegisteredCurse curse, final int level) {
        final String baseName = curse.definition().displayName();
        if (curse.getMaxLevel() == 1 && level == 1) {
            return baseName + "  ";
        }
        return baseName + " " + ROMAN.format(level);
    }

    private static final class LegacyRomanNumeral {
        private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        String format(final int number) {
            if (number <= 0) {
                return String.valueOf(number);
            }
            int remaining = number;
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < VALUES.length && remaining > 0; i++) {
                while (remaining >= VALUES[i]) {
                    builder.append(SYMBOLS[i]);
                    remaining -= VALUES[i];
                }
            }
            return builder.toString();
        }
    }
}
