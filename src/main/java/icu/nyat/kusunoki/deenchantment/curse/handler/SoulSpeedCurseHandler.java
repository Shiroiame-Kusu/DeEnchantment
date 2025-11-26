package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Grants bursts of speed on configured block types.
 */
public final class SoulSpeedCurseHandler extends AbstractCurseHandler {

    private static final UUID MODIFIER_ID = UUID.fromString("1ff16a9b-3b50-4ed4-b9f6-9e83418e8c1f");

    private final double speedRate;
    private final long periodTicks;
    private final Set<Material> boostBlocks;
    private final Map<UUID, SpeedWatcher> watchers = new HashMap<>();

    public SoulSpeedCurseHandler(final JavaPlugin plugin,
                                 final ConfigService configService,
                                 final EnchantTools enchantTools,
                                 final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.speedRate = Math.max(0D, configDouble(0.15D, "speed-rate", "speedRate"));
        this.periodTicks = Math.max(1L, configInt(10, "period"));
        this.boostBlocks = Collections.unmodifiableSet(resolveBlocks());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEquipmentChange(final DePlayerEquipmentChangeEvent event) {
        final Player player = event.getPlayer();
        final int level = getLevel(event);
        final boolean permitted = level > 0 && hasPermission(player);
        if (!permitted) {
            removeWatcher(player);
            return;
        }
        final double amount = level * speedRate;
        if (amount <= 0D) {
            removeWatcher(player);
            return;
        }
        final AttributeModifier modifier = new AttributeModifier(MODIFIER_ID, "De_Soul_Speed", amount,
                AttributeModifier.Operation.ADD_SCALAR);
        watchers.compute(player.getUniqueId(), (uuid, watcher) -> {
            if (watcher == null) {
                final SpeedWatcher created = new SpeedWatcher(player, modifier);
                created.runTaskTimer(plugin, 0L, periodTicks);
                return created;
            }
            watcher.setModifier(modifier);
            return watcher;
        });
    }

    private Set<Material> resolveBlocks() {
        final ConfigurationSection section = configuration();
        final List<String> configured = section == null ? List.of() : section.getStringList("blocks");
        final List<String> sources = configured == null || configured.isEmpty()
                ? List.of("DIRT", "GRASS_BLOCK")
                : configured;
        final Set<Material> materials = EnumSet.noneOf(Material.class);
        for (final String name : sources) {
            if (name == null || name.isEmpty()) {
                continue;
            }
            final Material material = Material.matchMaterial(name);
            if (material != null) {
                materials.add(material);
            }
        }
        if (materials.isEmpty()) {
            materials.add(Material.DIRT);
            materials.add(Material.GRASS_BLOCK);
        }
        return materials;
    }

    private void removeWatcher(final Player player) {
        if (player == null) {
            return;
        }
        final SpeedWatcher watcher = watchers.remove(player.getUniqueId());
        if (watcher != null) {
            watcher.cancel();
        }
    }

    @Override
    public void disable() {
        watchers.values().forEach(BukkitRunnable::cancel);
        watchers.clear();
    }

    private final class SpeedWatcher extends BukkitRunnable {

        private final Player player;
        private final AttributeInstance attribute;
        private AttributeModifier modifier;
        private boolean applied;

        private SpeedWatcher(final Player player, final AttributeModifier modifier) {
            this.player = Objects.requireNonNull(player, "player");
            this.modifier = Objects.requireNonNull(modifier, "modifier");
            this.attribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        }

        @Override
        public void run() {
            if (!player.isOnline() || player.isDead()) {
                cancel();
                watchers.remove(player.getUniqueId());
                return;
            }
            if (attribute == null) {
                cancel();
                watchers.remove(player.getUniqueId());
                return;
            }
            final Material blockType = player.getLocation().clone().add(0D, -0.5D, 0D).getBlock().getType();
            if (blockType.isAir()) {
                removeModifier();
                applied = false;
                return;
            }
            if (boostBlocks.contains(blockType)) {
                if (applied) {
                    return;
                }
                applied = true;
                attribute.removeModifier(modifier);
                attribute.addModifier(modifier);
            } else if (applied) {
                applied = false;
                attribute.removeModifier(modifier);
            }
        }

        private void removeModifier() {
            if (attribute != null) {
                attribute.removeModifier(modifier);
            }
        }

        public void setModifier(final AttributeModifier modifier) {
            if (attribute == null) {
                this.modifier = modifier;
                return;
            }
            attribute.removeModifier(this.modifier);
            this.modifier = modifier;
            if (applied) {
                attribute.addModifier(modifier);
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            removeModifier();
        }
    }
}
