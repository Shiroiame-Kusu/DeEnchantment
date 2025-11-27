package icu.nyat.kusunoki.deenchantment.curse.handler;

import icu.nyat.kusunoki.deenchantment.config.ConfigService;
import icu.nyat.kusunoki.deenchantment.curse.RegisteredCurse;
import icu.nyat.kusunoki.deenchantment.listener.event.DePlayerEquipmentChangeEvent;
import icu.nyat.kusunoki.deenchantment.util.item.EnchantTools;
import icu.nyat.kusunoki.deenchantment.util.text.PlaceholderText;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Implements the Binding Curse behaviour – items bind to their first owner and become unusable by others.
 */
public final class BindingCurseHandler extends AbstractCurseHandler {

    private final NamespacedKey bindingKey;
    private final boolean denyAnvil;
    private final boolean denyGrindstone;
    private final boolean denyPickup;
    private final boolean keepInventory;
    private final String bindMessage;
    private final String ownerMessage;
    private final String placeholder;

    public BindingCurseHandler(final JavaPlugin plugin,
                               final ConfigService configService,
                               final EnchantTools enchantTools,
                               final RegisteredCurse curse) {
        super(plugin, configService, enchantTools, curse);
        this.bindingKey = new NamespacedKey(plugin, "deenchantment_binding");
        this.denyAnvil = configBoolean(true, "deny-anvil", "denyAnvil");
        this.denyGrindstone = configBoolean(true, "deny-grindstone", "denyGrindStone");
        this.denyPickup = configBoolean(true, "deny-pickup", "denyPickup");
        this.keepInventory = configBoolean(true, "keep-inventory", "keepInventory");
        this.bindMessage = configString("&a您的装备已绑定您的灵魂", "bind-message", "bindMessage");
        this.ownerMessage = configString("&c你不能使用绑定了别人灵魂的装备!", "owner-message", "ownerMessage");
        this.placeholder = configString("玩家", "place-holder", "placeHolder");
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(final PlayerItemDamageEvent event) {
        final ItemStack item = event.getItem();
        if (!hasCurse(item)) {
            return;
        }
        final Player player = event.getPlayer();
        if (!hasPermission(player)) {
            return;
        }
        if (readBinding(item) != null) {
            return;
        }
        bindItem(item, player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (isForeign(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            sendOwnerMessage(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteract(final PlayerInteractEvent event) {
        final Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();
        if (!isForeign(player, inventory.getItemInMainHand()) && !isForeign(player, inventory.getItemInOffHand())) {
            return;
        }
        event.setCancelled(true);
        sendOwnerMessage(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAttack(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!isForeign(player, player.getInventory().getItemInMainHand())) {
            return;
        }
        event.setCancelled(true);
        sendOwnerMessage(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrindstone(final InventoryClickEvent event) {
        if (!denyGrindstone || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        final Inventory top = event.getView().getTopInventory();
        if (top == null || top.getType() != InventoryType.GRINDSTONE || event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }
        if (isForeign(player, top.getItem(0)) || isForeign(player, top.getItem(1))) {
            event.setCancelled(true);
            sendOwnerMessage(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnvil(final PrepareAnvilEvent event) {
        if (!denyAnvil) {
            return;
        }
        final HumanEntity viewer = event.getViewers().isEmpty() ? null : event.getViewers().get(0);
        if (!(viewer instanceof Player player)) {
            return;
        }
        final Inventory inventory = event.getInventory();
        checkAnvilSlot(player, inventory, 0);
        checkAnvilSlot(player, inventory, 1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEquipmentChange(final DePlayerEquipmentChangeEvent event) {
        final Player player = event.getPlayer();
        final ItemStack[] armors = event.getArmors();
        final List<ItemStack> removed = new ArrayList<>();
        for (int i = 0; i < armors.length; i++) {
            final ItemStack piece = armors[i];
            if (!isForeign(player, piece)) {
                continue;
            }
            removed.add(piece);
            armors[i] = null;
        }
        if (removed.isEmpty()) {
            return;
        }
        event.setArmors(armors);
        sendOwnerMessage(player);
        returnItems(player, removed);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(final PlayerDeathEvent event) {
        if (event.getKeepInventory() || !keepInventory) {
            return;
        }
        final Player player = event.getEntity();
        final Iterator<ItemStack> iterator = event.getDrops().iterator();
        final List<ItemStack> kept = new ArrayList<>();
        while (iterator.hasNext()) {
            final ItemStack stack = iterator.next();
            final BindingData data = readBinding(stack);
            if (data == null || !Objects.equals(data.id(), player.getUniqueId().toString())) {
                continue;
            }
            iterator.remove();
            kept.add(stack);
        }
        if (kept.isEmpty()) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> returnItems(player, kept));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(final EntityPickupItemEvent event) {
        if (!denyPickup) {
            return;
        }
        final BindingData data = readBinding(event.getItem().getItemStack());
        if (data == null) {
            return;
        }
        if (Objects.equals(data.id(), event.getEntity().getUniqueId().toString())) {
            return;
        }
        event.setCancelled(true);
    }

    private void bindItem(final ItemStack item, final Player player) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(bindingKey, PersistentDataType.STRING, player.getUniqueId() + ";" + player.getName());
        replacePlaceholder(meta, player.getName());
        item.setItemMeta(meta);
        sendMessage(player, bindMessage);
    }

    private boolean isForeign(final Player player, final ItemStack stack) {
        if (player == null || stack == null || stack.getType().isAir() || !hasCurse(stack)) {
            return false;
        }
        final BindingData data = readBinding(stack);
        if (data == null) {
            return false;
        }
        return !Objects.equals(data.id(), player.getUniqueId().toString());
    }

    private BindingData readBinding(final ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return null;
        }
        final ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        final String raw = meta.getPersistentDataContainer().get(bindingKey, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        final String[] parts = raw.split(";", 2);
        final String id = parts[0];
        final String name = parts.length > 1 ? parts[1] : id;
        return new BindingData(id, name);
    }

    private void checkAnvilSlot(final Player player, final Inventory inventory, final int slot) {
        final ItemStack stack = inventory.getItem(slot);
        if (!isForeign(player, stack)) {
            return;
        }
        inventory.setItem(slot, null);
        sendOwnerMessage(player);
        returnItems(player, List.of(stack));
    }

    private void returnItems(final Player player, final List<ItemStack> items) {
        final Inventory inventory = player.getInventory();
        for (final ItemStack stack : items) {
            final ItemStack clone = stack.clone();
            final var leftovers = inventory.addItem(clone);
            leftovers.values().forEach(remaining -> player.getWorld().dropItemNaturally(player.getLocation(), remaining));
        }
    }

    private void replacePlaceholder(final ItemMeta meta, final String playerName) {
        if (placeholder == null || placeholder.isEmpty() || !meta.hasLore()) {
            return;
        }
        final List<String> lore = meta.getLore();
        if (lore == null) {
            return;
        }
        boolean changed = false;
        for (int i = 0; i < lore.size(); i++) {
            final String line = lore.get(i);
            if (line == null) {
                continue;
            }
            if (line.contains(placeholder)) {
                lore.set(i, line.replace(placeholder, playerName));
                changed = true;
            }
        }
        if (changed) {
            meta.setLore(lore);
        }
    }

    private void sendOwnerMessage(final Player player) {
        sendMessage(player, ownerMessage);
    }

    private void sendMessage(final Player player, final String message) {
        if (player == null || message == null || message.isEmpty()) {
            return;
        }
        player.sendMessage(PlaceholderText.apply(player, message));
    }

    private record BindingData(String id, String name) {
        private BindingData {
            id = id == null ? "" : id.toLowerCase(Locale.ROOT);
        }
    }
}
