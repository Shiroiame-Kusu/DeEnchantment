package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.CurseId;
import icu.nyat.kusunoki.deenchantment.curse.CurseRegistry;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Coordinates the lifecycle of every curse-specific behaviour listener.
 */
public final class CurseBehaviorRegistry {

    private final JavaPlugin plugin;
    private final ConfigService configService;
    private final CurseRegistry curseRegistry;
    private final EnchantTools enchantTools;
    private final List<Listener> activeListeners = new ArrayList<>();

    public CurseBehaviorRegistry(final JavaPlugin plugin,
                                 final ConfigService configService,
                                 final CurseRegistry curseRegistry,
                                 final EnchantTools enchantTools) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.curseRegistry = Objects.requireNonNull(curseRegistry, "curseRegistry");
        this.enchantTools = Objects.requireNonNull(enchantTools, "enchantTools");
    }

    public void enable() {
        reload();
    }

    public void reload() {
        disable();
        registerHandlers();
    }

    public void disable() {
        for (Listener listener : activeListeners) {
            if (listener instanceof AbstractCurseHandler handler) {
                handler.disable();
            }
            HandlerList.unregisterAll(listener);
        }
        activeListeners.clear();
    }

    private void registerHandlers() {
        final PluginManager manager = plugin.getServer().getPluginManager();
        registerIfPresent(manager, CurseId.DE_PROTECTION,
                curse -> new ProtectionCurseHandler(plugin, configService, enchantTools, curse));
        registerIfPresent(manager, CurseId.DE_FIRE_PROTECTION,
                curse -> new FireProtectionCurseHandler(plugin, configService, enchantTools, curse));
        registerIfPresent(manager, CurseId.DE_BLAST_PROTECTION,
                curse -> new BlastProtectionCurseHandler(plugin, configService, enchantTools, curse));
        registerIfPresent(manager, CurseId.DE_PROJECTILE_PROTECTION,
            curse -> new ProjectileProtectionCurseHandler(plugin, configService, enchantTools, curse));
        registerIfPresent(manager, CurseId.DE_FEATHER_FALLING,
                curse -> new FeatherFallingCurseHandler(plugin, configService, enchantTools, curse));
        registerIfPresent(manager, CurseId.DE_RESPIRATION,
                curse -> new RespirationCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_AQUA_AFFINITY,
        curse -> new AquaAffinityCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_DEPTH_STRIDER,
        curse -> new DepthStriderCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_FROST_WALKER,
        curse -> new FrostWalkerCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_THORNS,
        curse -> new ThornsCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_BINDING_CURSE,
        curse -> new BindingCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_SHARPNESS,
        curse -> new SharpnessCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_SMITE,
        curse -> new SmiteCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_BANE_OF_ARTHROPODS,
        curse -> new BaneOfArthropodsCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_KNOCKBACK,
        curse -> new KnockbackCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_FIRE_ASPECT,
        curse -> new FireAspectCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_FLAME,
        curse -> new FlameCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_POWER,
        curse -> new PowerCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_PUNCH,
        curse -> new PunchCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_INFINITY,
        curse -> new InfinityCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_LOOTING,
        curse -> new LootingCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_SWEEPING,
        curse -> new SweepingCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_EFFICIENCY,
        curse -> new EfficiencyCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_UNBREAKING,
        curse -> new UnbreakingCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_FORTUNE,
        curse -> new FortuneCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_SILK_TOUCH,
        curse -> new SilkTouchCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_LUCK_OF_THE_SEA,
        curse -> new LuckOfTheSeaCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_LURE,
        curse -> new LureCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_LOYALTY,
        curse -> new LoyaltyCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_IMPALING,
        curse -> new ImpalingCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_RIPTIDE,
        curse -> new RiptideCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_CHANNELING,
        curse -> new ChannelingCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_MULTISHOT,
        curse -> new MultishotCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_QUICK_CHARGE,
        curse -> new QuickChargeCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_PIERCING,
        curse -> new PiercingCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_MENDING,
        curse -> new MendingCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_VANISHING_CURSE,
        curse -> new VanishingCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_SOUL_SPEED,
        curse -> new SoulSpeedCurseHandler(plugin, configService, enchantTools, curse));
    registerIfPresent(manager, CurseId.DE_SWIFT_SNEAK,
        curse -> new SwiftSneakCurseHandler(plugin, configService, enchantTools, curse));
    }

    private void registerIfPresent(final PluginManager manager,
                                   final CurseId id,
                                   final Function<RegisteredCurse, AbstractCurseHandler> factory) {
        curseRegistry.find(id).ifPresent(curse -> register(manager, factory.apply(curse)));
    }

    private void register(final PluginManager manager, final Listener listener) {
        manager.registerEvents(listener, plugin);
        activeListeners.add(listener);
    }
}
