package org.extstudios.extcore.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface InventoryBuilder {

    InventoryBuilder item(int slot, ItemStack item);

    InventoryBuilder item(int slot, ItemStack item, Consumer<InventoryClickEvent> clickHandler);

    InventoryBuilder fillEmpty(ItemStack item);

    InventoryBuilder fillRow(int row, ItemStack item);

    InventoryBuilder fillColumn(int column, ItemStack item);

    InventoryBuilder fillBorder(ItemStack item);

    InventoryBuilder onAnyClick(Consumer<InventoryClickEvent> clickHandler);

    InventoryBuilder onClose(Consumer<Player> closeHandler);

    InventoryBuilder cancelAllClicks();

    InventoryBuilder allowAllClicks();

    InventoryBuilder clearOnClose(boolean clearOnClose);

    Inventory build();

    Inventory openFor(Player player);
}