package org.extstudios.extcore.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

public interface ConfigService {

    boolean register(Object plugin, String fileName);

    boolean register(Object plugin);

    boolean reload(Object plugin, String fileName);

    int reloadAll(Object plugin);

    int reloadAll();

    boolean save(Object plugin, String fileName);

    int saveAll(Object plugin);

    String getString(String path, String defaultValue);

    String getString(String fileName, String path, String defaultValue);

    int getInt(String path, int defaultValue);

    int getInt(String fileName, String path, int defaultValue);

    double getDouble(String path, double defaultValue);

    double getDouble(String fileName, String path, double defaultValue);

    boolean getBoolean(String path, boolean defaultValue);

    boolean getBoolean(String fileName, String path, boolean defaultValue);

    List<String> getStringList(String path);

    List<String> getStringList(String fileName, String path);

    ConfigurationSection getSection(String path);

    ConfigurationSection getSection(String fileName, String path);

    Set<String> getKeys(String path, boolean deep);

    Set<String> getKeys(String fileName, String path, boolean deep);

    void set(String path, Object value);

    void set(String fileName, String path, Object value);

    boolean contains(String path);

    boolean contains(String fileName, String path);

    FileConfiguration getConfig(Object plugin, String fileName);

    FileConfiguration getConfig(Object plugin);

}