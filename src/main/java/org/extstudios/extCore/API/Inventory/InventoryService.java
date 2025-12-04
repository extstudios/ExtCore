package org.extstudios.extCore.API.Inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public interface InventoryService {

    InventoryBuilder create(Plugin plugin, String title, int rows);

    InventoryBuilder createWithSize(Plugin plugin, String title, int size);

    void open(Player player, Inventory inventory);

    void close(Player player);

    int getActiveInventoryCount();

    void unregisterAll(Plugin plugin);
}
