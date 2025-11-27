package icu.nyat.kusunoki.deenchantment.listener.controller;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.config.PluginConfig;
import icu.nyat.kusunoki.deenchantment.config.MessageConfig;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Registers and manages gameplay listeners that translate vanilla mechanics into curses.
 */
public final class GameplayControllerRegistry {

    private final JavaPlugin plugin;
    private final ConfigService configService;
    private final EnchantTools enchantTools;
    private final List<Listener> activeListeners = new ArrayList<>();

    public GameplayControllerRegistry(final JavaPlugin plugin,
                                      final ConfigService configService,
                                      final EnchantTools enchantTools) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.enchantTools = Objects.requireNonNull(enchantTools, "enchantTools");
    }

    public void enable() {
        registerControllers();
    }

    public void reload() {
        disable();
        enable();
    }

    public void disable() {
        for (Listener listener : activeListeners) {
            HandlerList.unregisterAll(listener);
        }
        activeListeners.clear();
    }

    private void registerControllers() {
        final PluginConfig config = configService.plugin();
        final MessageConfig messages = configService.messages();
        final PluginManager manager = plugin.getServer().getPluginManager();
        if (config.isEnableAnvil()) {
            register(manager, new AnvilController(configService, enchantTools, messages));
        }
        if (config.isEnableChestLoot()) {
            register(manager, new ChestLootController(enchantTools));
        }
        if (config.isEnableEnchanting()) {
            register(manager, new EnchantController(plugin, enchantTools, messages));
        }
        if (config.isEnableSpawn()) {
            register(manager, new EntitySpawnController(plugin, enchantTools));
        }
        if (config.isEnableTrade()) {
            register(manager, new MerchantController(enchantTools));
        }
        if (config.isEnableFishing()) {
            register(manager, new PlayerFishController(enchantTools));
        }
        if (config.isEnableRewardDrops()) {
            register(manager, new RewardDropController(enchantTools));
        }
        if (config.isEnableGrindstone()) {
            register(manager, new GrindstoneController(enchantTools));
        }
    }

    private void register(final PluginManager manager, final Listener listener) {
        manager.registerEvents(listener, plugin);
        activeListeners.add(listener);
    }
}
