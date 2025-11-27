package icu.nyat.kusunoki.deenchantment.bootstrap;

import icu.nyat.kusunoki.deenchantment.command.CommandRegistry;
import icu.nyat.kusunoki.deenchantment.command.subcommand.AddSubcommand;
import icu.nyat.kusunoki.deenchantment.command.subcommand.GiveSubcommand;
import icu.nyat.kusunoki.deenchantment.command.subcommand.MigrateSubcommand;
import icu.nyat.kusunoki.deenchantment.command.subcommand.PurificationSubcommand;
import icu.nyat.kusunoki.deenchantment.command.subcommand.RandomSubcommand;
import icu.nyat.kusunoki.deenchantment.command.subcommand.ReloadSubcommand;
import icu.nyat.kusunoki.deenchantment.command.subcommand.UpdateSubcommand;
import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.config.PluginConfig;
import icu.nyat.kusunoki.deenchantment.curse.CurseRegistry;
import icu.nyat.kusunoki.deenchantment.curse.handler.CurseBehaviorRegistry;
import icu.nyat.kusunoki.deenchantment.nms.NmsBridge;
import icu.nyat.kusunoki.deenchantment.nms.NmsBridgeLoader;
import icu.nyat.kusunoki.deenchantment.hook.HookManager;
import icu.nyat.kusunoki.deenchantment.listener.controller.EntityDeEnchantCaller;
import icu.nyat.kusunoki.deenchantment.listener.controller.GameplayControllerRegistry;
import icu.nyat.kusunoki.deenchantment.metrics.MetricsService;
import icu.nyat.kusunoki.deenchantment.task.EquipmentScanner;
import icu.nyat.kusunoki.deenchantment.task.TaskScheduler;
import icu.nyat.kusunoki.deenchantment.util.logging.PluginLogger;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Central wiring hub for all plugin services. {@link #load()} must be called
 * exactly once during {@code JavaPlugin#onLoad()}, followed by {@link #enable()}
 * during {@code onEnable()} and {@link #disable()} during {@code onDisable()}.
 */
public final class PluginContext {

    private final JavaPlugin plugin;

    private PluginLogger logger;
    private ConfigService configService;
    private TaskScheduler scheduler;
    private MetricsService metricsService;
    private CommandRegistry commandRegistry;
    private NmsBridge nmsBridge;
    private CurseRegistry curseRegistry;
    private EnchantTools enchantTools;
    private CurseBehaviorRegistry curseBehaviors;
    private EntityDeEnchantCaller entityDeEnchantCaller;
    private EquipmentScanner equipmentScanner;
    private GameplayControllerRegistry gameplayControllers;
    private HookManager hookManager;
    private boolean loaded;

    public PluginContext(final JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void load() {
        if (loaded) {
            return;
        }
        this.logger = new PluginLogger(plugin);
        this.configService = new ConfigService(plugin, logger);
        this.configService.load();
        this.scheduler = new TaskScheduler(plugin);
    this.metricsService = new MetricsService(plugin);
    this.commandRegistry = new CommandRegistry();
    final NmsBridgeLoader bridgeLoader = new NmsBridgeLoader(plugin, logger);
    this.nmsBridge = bridgeLoader.load();
    this.curseRegistry = new CurseRegistry(configService, logger, nmsBridge);
    this.enchantTools = new EnchantTools(plugin, configService, curseRegistry);
    this.curseBehaviors = new CurseBehaviorRegistry(plugin, configService, curseRegistry, enchantTools);
    this.entityDeEnchantCaller = new EntityDeEnchantCaller();
    this.equipmentScanner = new EquipmentScanner(scheduler);
    this.gameplayControllers = new GameplayControllerRegistry(plugin, configService, enchantTools);
    this.hookManager = new HookManager(plugin, logger, curseRegistry, enchantTools);
        registerDefaultCommands();
        this.loaded = true;
        updateDebugFlag();
        logger.info("Loaded configuration for " + plugin.getName() + " " + plugin.getPluginMeta().getVersion());
    }

    public void enable() {
        ensureLoaded();
        updateDebugFlag();
        logger.info("Starting DeEnchantment services");
        metricsService.start();
        commandRegistry.bind(plugin, this);
        curseRegistry.reload();
        curseBehaviors.enable();
    entityDeEnchantCaller.register(plugin);
    equipmentScanner.start();
    gameplayControllers.enable();
    hookManager.enable();
    }

    public void disable() {
        if (!loaded) {
            return;
        }
        logger.info("Shutting down DeEnchantment services");
        if (metricsService != null) {
            metricsService.stop();
        }
        if (scheduler != null) {
            scheduler.cancelAll();
        }
        if (curseRegistry != null) {
            curseRegistry.unregisterAll();
        }
        if (curseBehaviors != null) {
            curseBehaviors.disable();
        }
        if (entityDeEnchantCaller != null) {
            entityDeEnchantCaller.unregister();
        }
        if (equipmentScanner != null) {
            equipmentScanner.stop();
        }
        if (gameplayControllers != null) {
            gameplayControllers.disable();
        }
        if (hookManager != null) {
            hookManager.disable();
        }
    }

    public void reloadConfigs() {
        ensureLoaded();
    configService.reload();
    updateDebugFlag();
    curseRegistry.reload();
    curseBehaviors.reload();
    gameplayControllers.reload();
    hookManager.reload();
    logger.info("Reloaded DeEnchantment configuration");
    }

    private void updateDebugFlag() {
        if (configService == null || logger == null) {
            return;
        }
        final PluginConfig config = configService.plugin();
        logger.setDebugEnabled(config.isDebugLogging());
    }

    private void ensureLoaded() {
        if (!loaded) {
            throw new IllegalStateException("PluginContext has not been loaded yet");
        }
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    public PluginLogger logger() {
        ensureLoaded();
        return logger;
    }

    public ConfigService configs() {
        ensureLoaded();
        return configService;
    }

    public TaskScheduler scheduler() {
        ensureLoaded();
        return scheduler;
    }

    public MetricsService metrics() {
        ensureLoaded();
        return metricsService;
    }

    public CommandRegistry commands() {
        ensureLoaded();
        return commandRegistry;
    }

    public CurseRegistry curses() {
        ensureLoaded();
        return curseRegistry;
    }

    public EnchantTools enchantTools() {
        ensureLoaded();
        return enchantTools;
    }

    private void registerDefaultCommands() {
    commandRegistry
        .register(new ReloadSubcommand())
        .register(new GiveSubcommand())
        .register(new RandomSubcommand())
        .register(new AddSubcommand())
        .register(new UpdateSubcommand())
        .register(new PurificationSubcommand())
        .register(new MigrateSubcommand());
    }
}
