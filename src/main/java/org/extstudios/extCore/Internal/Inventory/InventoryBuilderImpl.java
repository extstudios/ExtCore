package org.extstudios.extCore.Internal.Inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.extstudios.extCore.API.Inventory.InventoryBuilder;

import java.util.function.Consumer;

public class InventoryBuilderImpl implements InventoryBuilder {

    private final InventoryServiceImpl service;
    private final String title;
    private final int size;
    private final CustomInventoryHolder holder;
    private Inventory inventory;

    public InventoryBuilderImpl(InventoryServiceImpl service, Plugin plugin, String title, int size) {
        this.service = service;
        this.title = title;
        this.size = size;
        this.holder = new CustomInventoryHolder(plugin);
        this.inventory = null; // Create lazily
    }

    private void ensureInventoryCreated() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(holder, size, title);
            holder.setInventory(inventory);
        }
    }

    @Override
    public InventoryBuilder item(int slot, ItemStack item) {
        return item(slot, item, null);
    }

    @Override
    public InventoryBuilder item(int slot, ItemStack item, Consumer<InventoryClickEvent> clickHandler) {
        ensureInventoryCreated();
        inventory.setItem(slot, item);

        if (clickHandler != null) {
            holder.addClickHandler(slot, clickHandler);
        }

        return this;
    }

    @Override
    public InventoryBuilder fillEmpty(ItemStack item) {
        ensureInventoryCreated();

        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item);
            }
        }

        return this;
    }

    @Override
    public InventoryBuilder fillRow(int row, ItemStack item) {
        ensureInventoryCreated();

        int start = row * 9;
        int end = Math.min(start + 9, size);

        for (int i = start; i < end; i++) {
            inventory.setItem(i, item);
        }

        return this;
    }

    @Override
    public InventoryBuilder fillColumn(int column, ItemStack item) {
        ensureInventoryCreated();

        for (int row = 0; row < (size / 9); row++) {
            int slot = (row * 9) + column;
            if (slot < size) {
                inventory.setItem(slot, item);
            }
        }

        return this;
    }

    @Override
    public InventoryBuilder fillBorder(ItemStack item) {
        ensureInventoryCreated();

        int rows = size / 9;

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, item); // Top
            inventory.setItem((rows - 1) * 9 + i, item); // Bottom
        }

        for (int row = 1; row < rows - 1; row++) {
            inventory.setItem(row * 9, item); // Left
            inventory.setItem(row * 9 + 8, item); // Right
        }

        return this;
    }

    @Override
    public InventoryBuilder onAnyClick(Consumer<InventoryClickEvent> clickHandler) {
        holder.setDefaultClickHandler(clickHandler);
        return this;
    }

    @Override
    public InventoryBuilder onClose(Consumer<Player> closeHandler) {
        holder.setCloseHandler(closeHandler);
        return this;
    }

    @Override
    public InventoryBuilder cancelAllClicks() {
        holder.setCancelAllClicks(true);
        return this;
    }

    @Override
    public InventoryBuilder allowAllClicks() {
        holder.setCancelAllClicks(false);
        return this;
    }

    @Override
    public InventoryBuilder clearOnClose(boolean clearOnClose) {
        holder.setClearOnClose(clearOnClose);
        return this;
    }

    @Override
    public Inventory build() {
        ensureInventoryCreated();

        service.registerInventory(holder);

        return inventory;
    }

    @Override
    public Inventory openFor(Player player) {
        Inventory inv = build();
        service.open(player, inv);
        return inv;
    }
}