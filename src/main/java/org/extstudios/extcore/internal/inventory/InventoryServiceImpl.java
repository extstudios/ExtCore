package org.extstudios.extcore.internal.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.extstudios.extcore.api.inventory.InventoryBuilder;
import org.extstudios.extcore.api.inventory.InventoryService;
import org.extstudios.extcore.api.LoggingService;
import org.extstudios.extcore.api.task.TaskService;
import org.extstudios.extcore.internal.Platform;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InventoryServiceImpl implements InventoryService, Listener {

    private final Plugin plugin;
    private final LoggingService logger;
    private final TaskService taskService;
    private final boolean isFolia;

    private final Map<UUID, CustomInventoryHolder> activeInventories;
    private final Map<Plugin, Set<CustomInventoryHolder>> pluginHolders;

    public InventoryServiceImpl(Plugin plugin, LoggingService logger, TaskService taskService) {
        this.plugin = plugin;
        this.logger = logger.withPrefix("[Inventory]");
        this.taskService = taskService;
        this.isFolia = Platform.isFolia();
        this.activeInventories = new ConcurrentHashMap<>();
        this.pluginHolders = new ConcurrentHashMap<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);

        if (isFolia) {
            logger.debug("InventoryService initialized (Folia mode - entity threading)");
        } else {
            logger.debug("InventoryService initialized (Paper mode - direct execution)");
        }
    }

    @Override
    public InventoryBuilder create(Plugin plugin, String title, int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6");
        }
        return createWithSize(plugin, title, rows * 9);
    }

    @Override
    public InventoryBuilder createWithSize(Plugin plugin, String title, int size) {
        if (size % 9 != 0 || size < 9 || size > 54) {
            throw new IllegalArgumentException("Size must be a multiple of 9 between 9 and 54");
        }

        return new InventoryBuilderImpl(this, plugin, title, size);
    }

    @Override
    public void open(Player player, Inventory inventory) {
        if (isFolia) {
            taskService.runAtEntity(plugin, player, () -> openInventoryDirect(player, inventory));
        } else {
            // Paper: Run directly (already on main thread)
            openInventoryDirect(player, inventory);
        }
    }

    @Override
    public void close(Player player) {
        if (isFolia) {
            // Folia: Use entity thread for thread safety
            taskService.runAtEntity(plugin, player, player::closeInventory);
        } else {
            // Paper: Run directly
            player.closeInventory();
        }
    }

    @Override
    public int getActiveInventoryCount() {
        return activeInventories.size();
    }

    @Override
    public void unregisterAll(Plugin plugin) {
        Set<CustomInventoryHolder> holders = pluginHolders.remove(plugin);
        if (holders != null) {
            logger.info("Unregistered", holders.size(), "inventories for", plugin.getName());
        }
    }

    private void openInventoryDirect(Player player, Inventory inventory) {
        player.openInventory(inventory);

        if (inventory.getHolder() instanceof CustomInventoryHolder holder) {
            activeInventories.put(player.getUniqueId(), holder);
            logger.debug("Opened custom inventory for", player.getName());
        }
    }

    public void registerInventory(CustomInventoryHolder holder) {
        pluginHolders.computeIfAbsent(holder.getPlugin(), k -> ConcurrentHashMap.newKeySet()).add(holder);
        logger.debug("Registered inventory for", holder.getPlugin().getName());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getClickedInventory();
        if (inventory == null) {
            return;
        }

        if (!(inventory.getHolder() instanceof CustomInventoryHolder holder)) {
            return;
        }

        if (holder.isCancelAllClicks()) {
            event.setCancelled(true);
        }

        int slot = event.getSlot();
        Consumer<InventoryClickEvent> specificHandler = holder.getClickHandler(slot);

        if (specificHandler != null) {
            try {
                specificHandler.accept(event);
            } catch (Exception e) {
                logger.error(e, "Error in click handler for slot", slot);
            }
        } else {
            Consumer<InventoryClickEvent> defaultHandler = holder.getDefaultClickHandler();
            if (defaultHandler != null) {
                try {
                    defaultHandler.accept(event);
                } catch (Exception e) {
                    logger.error(e, "Error in default click handler");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof CustomInventoryHolder holder)) {
            return;
        }

        activeInventories.remove(player.getUniqueId());

        Consumer<Player> closeHandler = holder.getCloseHandler();
        if (closeHandler != null) {
            try {
                closeHandler.accept(player);
            } catch (Exception e) {
                logger.error(e, "Error in close handler");
            }
        }

        if (holder.isClearOnClose()) {
            inventory.clear();
        }

        logger.debug("Closed custom inventory for", player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        CustomInventoryHolder holder = activeInventories.remove(uuid);

        if (holder != null) {
            logger.debug("Cleaned up inventory for", event.getPlayer().getName(), "on quit");
        }
    }

    public void shutdown() {
        logger.info("Shutting down InventoryService...");

        // Close all active inventories
        activeInventories.forEach((uuid, holder) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                if (isFolia) {
                    taskService.runAtEntity(plugin, player, player::closeInventory);
                } else {
                    player.closeInventory();
                }
            }
        });

        activeInventories.clear();
        pluginHolders.clear();

        logger.info("InventoryService shutdown complete");
    }

}