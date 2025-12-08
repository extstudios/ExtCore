package org.extstudios.extcore.api;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public interface PlayerDataService {

    <T> void set(Player player, String key, T value);

    <T> void set(UUID uuid, String key, T value);

    <T> T get(Player player, String key, Class<T> type);

    <T> T get(UUID uuid, String key, Class<T> type);

    <T> T get(Player player, String key, T defaultValue);

    <T> T get(UUID uuid, String key, T defaultValue);

    boolean has(Player player, String key);

    boolean has(UUID uuid, String key);

    void remove(Player player, String key);

    void remove(UUID uuid, String key);

    void clear(Player player);

    void clear(UUID uuid);

    Map<String, Object> getAll(Player player);

    Map<String, Object> getAll(UUID uuid);

    void save(Player player);

    void save(UUID uuid);

    void saveAll();

    int getCachedPlayerCount();

    boolean isCached(Player player);

    boolean isCached(UUID uuid);
}