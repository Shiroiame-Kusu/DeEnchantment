package icu.nyat.kusunoki.deenchantment.curse;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum CurseId {
    DE_PROTECTION("de_protection", "§7保护不了", "§8 - 增加受到的伤害", EnchantmentTarget.ARMOR, 4, 1, false, true,
            Set.of("de_blast_protection", "de_projectile_protection", "de_fire_protection")),
    DE_FIRE_PROTECTION("de_fire_protection", "§7易燃", "§8 - 受到攻击时有概率着火", EnchantmentTarget.ARMOR, 4, 1, false, true,
            Set.of("de_blast_protection", "de_projectile_protection", "de_protection")),
    DE_BLAST_PROTECTION("de_blast_protection", "§7瞬间爆炸", "§8 - 受到伤害时有概率爆炸", EnchantmentTarget.ARMOR, 4, 1, false, true,
            Set.of("de_fire_protection", "de_projectile_protection", "de_protection")),
    DE_PROJECTILE_PROTECTION("de_projectile_protection", "§7弹射物吸引", "§8 - 吸引附近的弹射物", EnchantmentTarget.ARMOR, 4, 1, false, true,
            Set.of("de_fire_protection", "de_blast_protection", "de_protection")),
    DE_FEATHER_FALLING("de_feather_falling", "§7摔落骨折", "§8 - 增加受到的摔落伤害", EnchantmentTarget.ARMOR_FEET, 4, 1, false, true,
            Set.of()),
    DE_RESPIRATION("de_respiration", "§7水下窒息", "§8 - 消耗氧气的速度增加", EnchantmentTarget.ARMOR_HEAD, 3, 1, false, true,
            Set.of()),
    DE_AQUA_AFFINITY("de_aqua_affinity", "§7水下慢掘", "§8 - 水下挖掘有概率失败", EnchantmentTarget.ARMOR_HEAD, 1, 1, false, true,
            Set.of()),
    DE_THORNS("de_thorns", "§7负荆请罪", "§8 - 疾跑时有概率受伤", EnchantmentTarget.ARMOR, 3, 1, false, true,
            Set.of()),
    DE_DEPTH_STRIDER("de_depth_strider", "§7旱鸭子", "§8 - 水下视野减少", EnchantmentTarget.ARMOR_FEET, 3, 1, false, true,
            Set.of("de_frost_walker")),
    DE_FROST_WALKER("de_frost_walker", "§7熔岩行者", "§8 - 在岩浆上行走", EnchantmentTarget.ARMOR_FEET, 2, 1, false, true,
            Set.of("de_depth_strider")),
    DE_BINDING_CURSE("de_binding_curse", "§a灵魂绑定", "§8 - 绑定玩家的灵魂仅允许使用", EnchantmentTarget.BREAKABLE, 1, 1, true, true,
            Set.of("de_vanishing_curse")),
    DE_SHARPNESS("de_sharpness", "§7磨钝", "§8 - 攻击伤害减少", EnchantmentTarget.WEAPON, 5, 1, false, true,
            Set.of("de_smite", "de_bane_of_arthropods")),
    DE_SMITE("de_smite", "§7亡灵之友", "§8 - 对亡灵生物伤害减少", EnchantmentTarget.WEAPON, 5, 1, false, true,
            Set.of("de_sharpness", "de_bane_of_arthropods")),
    DE_BANE_OF_ARTHROPODS("de_bane_of_arthropods", "§7截肢之友", "§8 - 对截肢生物伤害减少", EnchantmentTarget.WEAPON, 5, 1, false, true,
            Set.of("de_sharpness", "de_smite")),
    DE_KNOCKBACK("de_knockback", "§7退击", "§8 - 攻击时后跳一段距离", EnchantmentTarget.WEAPON, 2, 1, false, true,
            Set.of()),
    DE_FIRE_ASPECT("de_fire_aspect", "§7引火烧身", "§8 - 攻击时有概率烧伤自己", EnchantmentTarget.WEAPON, 2, 1, false, true,
            Set.of()),
    DE_LOOTING("de_looting", "§7知足", "§8 - 击杀生物有概率没有掉落物", EnchantmentTarget.WEAPON, 3, 1, false, true,
            Set.of()),
    DE_SWEEPING("de_sweeping", "§7牵引之刃", "§8 - 将范围内的敌人吸引过来", EnchantmentTarget.WEAPON, 3, 1, false, true,
            Set.of()),
    DE_EFFICIENCY("de_efficiency", "§7低效", "§8 - 有概率挖掘失败", EnchantmentTarget.TOOL, 5, 1, false, true,
            Set.of()),
    DE_SILK_TOUCH("de_silk_touch", "§7彻底粉碎", "§8 - 挖掘方块不会有掉落物", EnchantmentTarget.TOOL, 1, 1, false, true,
            Set.of("de_fortune")),
    DE_UNBREAKING("de_unbreaking", "§7易损", "§8 - 增加耐久消耗", EnchantmentTarget.BREAKABLE, 3, 1, false, true,
            Set.of()),
    DE_FORTUNE("de_fortune", "§7霉运", "§8 - 挖掘方块掉落物有概率消失", EnchantmentTarget.TOOL, 3, 1, false, true,
            Set.of("de_silk_touch")),
    DE_POWER("de_power", "§7虚弱", "§8 - 箭矢伤害和速度减少", EnchantmentTarget.BOW, 5, 1, false, true,
            Set.of()),
    DE_PUNCH("de_punch", "§7拉扯", "§8 - 将目标拉扯过来", EnchantmentTarget.BOW, 2, 1, false, true,
            Set.of()),
    DE_FLAME("de_flame", "§7神速", "§8 - 箭矢速度增加", EnchantmentTarget.BOW, 1, 1, false, true,
            Set.of()),
    DE_INFINITY("de_infinity", "§7多重", "§8 - 消耗一根箭射出多支箭", EnchantmentTarget.BOW, 2, 1, false, true,
            Set.of("de_mending")),
    DE_LUCK_OF_THE_SEA("de_luck_of_the_sea", "§7海之嫌弃", "§8 - 钓到的东西有概率脱钩", EnchantmentTarget.FISHING_ROD, 3, 1, false, true,
            Set.of()),
    DE_LURE("de_lure", "§7过期钓饵", "§8 - 增加鱼上钩的时间", EnchantmentTarget.FISHING_ROD, 3, 1, false, true,
            Set.of()),
    DE_LOYALTY("de_loyalty", "§7背叛", "§8 - 三叉戟有概率叛逃他人", EnchantmentTarget.TRIDENT, 3, 1, false, true,
            Set.of("de_riptide")),
    DE_IMPALING("de_impaling", "§7刺穿", "§8 - 增加对非水生生物的伤害", EnchantmentTarget.TRIDENT, 5, 1, false, true,
            Set.of()),
    DE_RIPTIDE("de_riptide", "§7焰流", "§8 - 在燃烧时快速移动", EnchantmentTarget.TRIDENT, 3, 1, false, true,
            Set.of("de_loyalty", "de_channeling")),
    DE_CHANNELING("de_channeling", "§7引雷针", "§8 - 在雷雨天时会遭天谴", EnchantmentTarget.TRIDENT, 1, 1, false, true,
            Set.of("de_riptide")),
    DE_MULTISHOT("de_multishot", "§7连珠", "§8 - 连续射出多支箭", EnchantmentTarget.CROSSBOW, 1, 1, false, true,
            Set.of("de_piercing")),
    DE_QUICK_CHARGE("de_quick_charge", "§7慢速装填", "§8 - 填充速度减慢", EnchantmentTarget.CROSSBOW, 3, 1, false, true,
            Set.of("de_piercing")),
    DE_PIERCING("de_piercing", "§7反弹", "§8 - 击中生物时反弹", EnchantmentTarget.CROSSBOW, 4, 1, false, true,
            Set.of("de_multishot")),
    DE_MENDING("de_mending", "§7经验反哺", "§8 - 消耗耐久增加经验", EnchantmentTarget.BREAKABLE, 1, 1, false, true,
            Set.of()),
    DE_VANISHING_CURSE("de_vanishing_curse", "§a永存祝福", "§8 - 物品将永远陪伴你", EnchantmentTarget.BREAKABLE, 1, 1, true, true,
            Set.of("de_binding_curse")),
    DE_SOUL_SPEED("de_soul_speed", "§7大地疾行", "§8 - 在土地上疾行", EnchantmentTarget.ARMOR_FEET, 3, 1, false, true,
            Set.of()),
    DE_SWIFT_SNEAK("de_swift_sneak", "§7沉重步伐", "§8 - 缓慢前行但抗性提升", EnchantmentTarget.ARMOR_LEGS, 3, 1, true, true,
            Set.of());

    private static final Map<String, CurseId> BY_KEY = new ConcurrentHashMap<>();

    static {
        Arrays.stream(values()).forEach(id -> BY_KEY.put(id.key, id));
    }

    private final String key;
    private final String defaultName;
    private final String defaultDescription;
    private final EnchantmentTarget target;
    private final int maxLevel;
    private final int startLevel;
    private final boolean treasure;
    private final boolean cursed;
    private final Set<String> defaultConflicts;

    CurseId(final String key,
            final String defaultName,
            final String defaultDescription,
            final EnchantmentTarget target,
            final int maxLevel,
            final int startLevel,
            final boolean treasure,
            final boolean cursed,
            final Set<String> defaultConflicts) {
        this.key = key;
        this.defaultName = defaultName;
        this.defaultDescription = defaultDescription;
        this.target = target;
        this.maxLevel = maxLevel;
        this.startLevel = startLevel;
        this.treasure = treasure;
        this.cursed = cursed;
        this.defaultConflicts = defaultConflicts;
    }

    public String key() {
        return key;
    }

        public NamespacedKey namespacedKey() {
                return NamespacedKey.minecraft(key);
        }

        public String vanillaKey() {
                if (key.startsWith("de_") && key.length() > 3) {
                        return key.substring(3);
                }
                return key;
        }

        public NamespacedKey vanillaNamespacedKey() {
                return NamespacedKey.minecraft(vanillaKey());
        }

        public Optional<Enchantment> vanillaEnchantment() {
                return Optional.ofNullable(Enchantment.getByKey(vanillaNamespacedKey()));
    }

    public String defaultName() {
        return defaultName;
    }

    public String defaultDescription() {
        return defaultDescription;
    }

    public EnchantmentTarget defaultTarget() {
        return target;
    }

    public int defaultMaxLevel() {
        return maxLevel;
    }

    public int defaultStartLevel() {
        return startLevel;
    }

    public boolean defaultTreasure() {
        return treasure;
    }

    public boolean defaultCursed() {
        return cursed;
    }

    public Set<String> defaultConflicts() {
        return defaultConflicts;
    }

    public static Optional<CurseId> fromKey(final String key) {
        return Optional.ofNullable(BY_KEY.get(key.toLowerCase(Locale.ROOT)));
    }
}
