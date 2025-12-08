package org.extstudios.extcore.internal.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.extstudios.extcore.api.LoggingService;
import org.extstudios.extcore.api.PlayerDataService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataServiceImpl implements PlayerDataService, Listener {

    private final LoggingService logger;
    private final Map<UUID, PlayerData> playerDataCache;
    private final boolean autoSaveOnQuit;

    public PlayerDataServiceImpl(Plugin plugin, LoggingService logger) {
        this.logger = logger.withPrefix("[PlayerData]");
        this.playerDataCache = new ConcurrentHashMap<>();
        this.autoSaveOnQuit = true;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        logger.debug("PlayerDataService initialized");
    }

    @Override
    public <T> void set(Player player, String key, T value) {
        set(player.getUniqueId(), key, value);
    }

    @Override
    public <T> void set(UUID uuid, String key, T value) {
        PlayerData data = getOrCreateData(uuid);
        data.set(key, value);
        logger.debug("Set", key, "for", uuid);
    }

    @Override
    public <T> T get(Player player, String key, Class<T> type) {
        return get(player.getUniqueId(), key, type);
    }

    @Override
    public <T> T get(UUID uuid, String key, Class<T> type) {
        PlayerData data = playerDataCache.get(uuid);
        if (data == null) {
            return null;
        }
        return data.get(key, type);
    }

    @Override
    public <T> T get(Player player, String key, T defaultValue) {
        return get(player.getUniqueId(), key, defaultValue);
    }

    @Override
    public <T> T get(UUID uuid, String key, T defaultValue) {
        PlayerData data = playerDataCache.get(uuid);
        if (data == null) {
            return defaultValue;
        }
        return data.get(key, defaultValue);
    }

    @Override
    public boolean has(Player player, String key) {
        return has(player.getUniqueId(), key);
    }

    @Override
    public boolean has(UUID uuid, String key) {
        PlayerData data = playerDataCache.get(uuid);
        return data != null && data.has(key);
    }

    @Override
    public void remove(Player player, String key) {
        remove(player.getUniqueId(), key);
    }

    @Override
    public void remove(UUID uuid, String key) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            data.remove(key);
            logger.debug("Removed", key, "for", uuid);
        }
    }

    @Override
    public void clear(Player player) {
        clear(player.getUniqueId());
    }

    @Override
    public void clear(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            data.clear();
            logger.debug("Cleared all data for", uuid);
        }
    }

    @Override
    public Map<String, Object> getAll(Player player) {
        return getAll(player.getUniqueId());
    }

    @Override
    public Map<String, Object> getAll(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data == null) {
            return Map.of();
        }
        return data.getAll();
    }

    @Override
    public void save(Player player) {
        save(player.getUniqueId());
    }

    @Override
    public void save(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null && data.isDirty()) {
            // TODO: Implement persistence to file/database
            // For now, just marking as clean
            data.markClean();
            logger.debug("Saved data for", uuid);
        }
    }

    @Override
    public void saveAll() {
        int saved = 0;
        for (PlayerData data : playerDataCache.values()) {
            if (data.isDirty()) {
                save(data.getUUID());
                saved++;
            }
        }
        if (saved > 0) {
            logger.info("Saved data for", saved, "player(s)");
        }
    }

    @Override
    public int getCachedPlayerCount() {
        return playerDataCache.size();
    }

    @Override
    public boolean isCached(Player player) {
        return isCached(player.getUniqueId());
    }

    @Override
    public boolean isCached(UUID uuid) {
        return playerDataCache.containsKey(uuid);
    }

    private PlayerData getOrCreateData(UUID uuid) {
        return playerDataCache.computeIfAbsent(uuid, PlayerData::new);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData data = playerDataCache.get(uuid);

        if (data == null) {
            return;
        }

        if (autoSaveOnQuit && data.isDirty()) {
            save(uuid);
        }

        playerDataCache.remove(uuid);
        logger.debug("Cleaned up data for", uuid, "on quit");
    }

    public void shutdown() {
        logger.info("Shutting down PlayerDataService...");
        saveAll();
        playerDataCache.clear();
        logger.info("PlayerDataService shutdown complete");
    }
}