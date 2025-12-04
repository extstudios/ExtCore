package org.extstudios.extCore.Internal.Inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CustomInventoryHolder implements InventoryHolder {

    private final Plugin plugin;
    private Inventory inventory;

    private final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers;

    private Consumer<InventoryClickEvent> defaultClickHandler;
    private Consumer<Player> closeHandler;
    private boolean cancelAllClicks;
    private boolean clearOnClose;

    public CustomInventoryHolder(Plugin plugin) {
        this.plugin = plugin;
        this.clickHandlers = new HashMap<>();
        this.cancelAllClicks = true;
        this.clearOnClose = false;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void addClickHandler(int slot, Consumer<InventoryClickEvent> handler) {
        clickHandlers.put(slot, handler);
    }

    public Consumer<InventoryClickEvent> getClickHandler(int slot) {
        return clickHandlers.get(slot);
    }

    public void setDefaultClickHandler(Consumer<InventoryClickEvent> handler) {
        this.defaultClickHandler = handler;
    }

    public Consumer<InventoryClickEvent> getDefaultClickHandler() {
        return defaultClickHandler;
    }

    public void setCloseHandler(Consumer<Player> handler) {
        this.closeHandler = handler;
    }

    public Consumer<Player> getCloseHandler() {
        return closeHandler;
    }

    public void setCancelAllClicks(boolean cancelAllClicks) {
        this.cancelAllClicks = cancelAllClicks;
    }

    public boolean isCancelAllClicks() {
        return cancelAllClicks;
    }

    public void setClearOnClose(boolean clearOnClose) {
        this.clearOnClose = clearOnClose;
    }

    public boolean isClearOnClose() {
        return clearOnClose;
    }
}
