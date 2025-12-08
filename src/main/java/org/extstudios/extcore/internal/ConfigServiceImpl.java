package org.extstudios.extcore.internal;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.extstudios.extcore.api.ConfigService;
import org.extstudios.extcore.api.LoggingService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigServiceImpl implements ConfigService {

    private final LoggingService logger;
    private final Map<Plugin, Map<String, FileConfiguration>> configs;
    private final Map<Plugin, Map<String, File>> configFiles;
    private final ThreadLocal<Plugin> contextPlugin;

    public ConfigServiceImpl(LoggingService logger) {
        this.logger = logger.withPrefix("[Config]");
        this.configs = new ConcurrentHashMap<>();
        this.configFiles = new ConcurrentHashMap<>();
        this.contextPlugin = new ThreadLocal<>();
    }

    @Override
    public boolean register(Object plugin, String fileName) {
        if (!(plugin instanceof Plugin pluginInstance)) {
            logger.error("Invalid plugin instance:", plugin);
            return false;
        }

        contextPlugin.set(pluginInstance);

        configs.putIfAbsent(pluginInstance, new ConcurrentHashMap<>());
        configFiles.putIfAbsent(pluginInstance, new ConcurrentHashMap<>());

        Map<String, FileConfiguration> pluginConfigs = configs.get(pluginInstance);
        Map<String, File> pluginFiles = configFiles.get(pluginInstance);

        if (pluginConfigs.containsKey(fileName)) {
            logger.debug("Config already registered:", fileName, "for", pluginInstance.getName());
            return false;
        }

        File configFile = new File(pluginInstance.getDataFolder(), fileName);

        if (!configFile.exists()) {
            createFromResource(pluginInstance, fileName, configFile);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        loadDefaults(pluginInstance, fileName, config);

        pluginConfigs.put(fileName, config);
        pluginFiles.put(fileName, configFile);

        logger.debug("Registered config:", fileName, "for", pluginInstance.getName());
        return true;
    }

    @Override
    public boolean register(Object plugin) {
        return register(plugin, "config.yml");
    }

    @Override
    public boolean reload(Object plugin, String fileName) {
        if (!(plugin instanceof Plugin pluginInstance)) {
            return false;
        }

        Map<String, FileConfiguration> pluginConfigs = configs.get(pluginInstance);
        Map<String, File> pluginFiles = configFiles.get(pluginInstance);

        if (pluginConfigs == null || !pluginConfigs.containsKey(fileName)) {
            logger.warn("Attempted to reload unregistered config:", fileName);
            return false;
        }

        File configFile = pluginFiles.get(fileName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        loadDefaults(pluginInstance, fileName, config);

        pluginConfigs.put(fileName, config);

        logger.debug("Reloaded config:", fileName, "for", pluginInstance.getName());
        return true;
    }

    @Override
    public int reloadAll(Object plugin) {
        if (!(plugin instanceof Plugin pluginInstance)) {
            return 0;
        }

        Map<String, FileConfiguration> pluginConfigs = configs.get(pluginInstance);

        if (pluginConfigs == null) {
            return 0;
        }

        int count = 0;
        for (String fileName : pluginConfigs.keySet()) {
            if (reload(pluginInstance, fileName)) {
                count++;
            }
        }

        logger.info("Reloaded", count, "config(s) for", pluginInstance.getName());
        return count;
    }

    @Override
    public int reloadAll() {
        int total = 0;
        for (Plugin plugin : configs.keySet()) {
            total += reloadAll(plugin);
        }
        logger.info("Reloaded", total, "total config(s)");
        return total;
    }

    @Override
    public boolean save(Object plugin, String fileName) {
        if (!(plugin instanceof Plugin pluginInstance)) {
            return false;
        }

        Map<String, FileConfiguration> pluginConfigs = configs.get(pluginInstance);
        Map<String, File> pluginFiles = configFiles.get(pluginInstance);

        if (pluginConfigs == null || !pluginConfigs.containsKey(fileName)) {
            logger.warn("Attempted to save unregistered config:", fileName);
            return false;
        }

        try {
            FileConfiguration config = pluginConfigs.get(fileName);
            File configFile = pluginFiles.get(fileName);
            config.save(configFile);
            logger.debug("Saved config:", fileName, "for", pluginInstance.getName());
            return true;
        } catch (IOException e) {
            logger.error(e, "Failed to save config:", fileName);
            return false;
        }
    }

    @Override
    public int saveAll(Object plugin) {
        if (!(plugin instanceof Plugin pluginInstance)) {
            return 0;
        }

        Map<String, FileConfiguration> pluginConfigs = configs.get(pluginInstance);

        if (pluginConfigs == null) {
            return 0;
        }

        int count = 0;
        for (String fileName : pluginConfigs.keySet()) {
            if (save(pluginInstance, fileName)) {
                count++;
            }
        }

        return count;
    }

    @Override
    public String getString(String path, String defaultValue) {
        return getString("config.yml", path, defaultValue);
    }

    @Override
    public String getString(String fileName, String path, String defaultValue) {
        FileConfiguration config = getActiveConfig(fileName);
        return config != null ? config.getString(path, defaultValue) : defaultValue;
    }

    @Override
    public int getInt(String path, int defaultValue) {
        return getInt("config.yml", path, defaultValue);
    }

    @Override
    public int getInt(String fileName, String path, int defaultValue) {
        FileConfiguration config = getActiveConfig(fileName);
        return config != null ? config.getInt(path, defaultValue) : defaultValue;
    }

    @Override
    public double getDouble(String path, double defaultValue) {
        return getDouble("config.yml", path, defaultValue);
    }

    @Override
    public double getDouble(String fileName, String path, double defaultValue) {
        FileConfiguration config = getActiveConfig(fileName);
        return config != null ? config.getDouble(path, defaultValue) : defaultValue;
    }

    @Override
    public boolean getBoolean(String path, boolean defaultValue) {
        return getBoolean("config.yml", path, defaultValue);
    }

    @Override
    public boolean getBoolean(String fileName, String path, boolean defaultValue) {
        FileConfiguration config = getActiveConfig(fileName);
        return config != null ? config.getBoolean(path, defaultValue) : defaultValue;
    }

    @Override
    public List<String> getStringList(String path) {
        return getStringList("config.yml", path);
    }

    @Override
    public List<String> getStringList(String fileName, String path) {
        FileConfiguration config = getActiveConfig(fileName);
        return config != null ? config.getStringList(path) : Collections.emptyList();
    }

    @Override
    public ConfigurationSection getSection(String path) {
        return getSection("config.yml", path);
    }

    @Override
    public ConfigurationSection getSection(String fileName, String path) {
        FileConfiguration config = getActiveConfig(fileName);
        return config != null ? config.getConfigurationSection(path) : null;
    }

    @Override
    public Set<String> getKeys(String path, boolean deep) {
        return getKeys("config,yml", path, deep);
    }

    @Override
    public Set<String> getKeys(String fileName, String path, boolean deep) {
        ConfigurationSection section = getSection(fileName, path);
        return section != null ? section.getKeys(deep) : Collections.emptySet();
    }

    @Override
    public void set(String path, Object value) {
        set("config.yml", path, value);
    }

    public void set(String fileName, String path, Object value) {
        FileConfiguration config = getActiveConfig(fileName);
        if (config != null) {
            config.set(path, value);
        }
    }

    @Override
    public boolean contains(String path) {
        return contains("config.yml", path);
    }

    @Override
    public boolean contains(String fileName, String path) {
        FileConfiguration config = getActiveConfig(fileName);
        return config != null && config.contains(path);
    }

    @Override
    public FileConfiguration getConfig(Object plugin, String fileName) {
        if (!(plugin instanceof Plugin pluginInstance)) {
            return null;
        }

        Map<String, FileConfiguration> pluginConfigs = configs.get(pluginInstance);
        return pluginConfigs != null ? pluginConfigs.get(fileName) : null;
    }

    @Override
    public FileConfiguration getConfig(Object plugin) {
        return getConfig(plugin, "config.yml");
    }

    private FileConfiguration getActiveConfig(String fileName) {
        Plugin plugin = contextPlugin.get();
        if (plugin == null) {
            logger.error("No plugin context set! Call register() first or use methods that take plugin parameter.");
            return null;
        }
        return getConfig(plugin, fileName);
    }

    private void createFromResource(Plugin plugin, String fileName, File configFile) {
        try {
            File parentDir = configFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    logger.warn("Failed to create parent directories for", fileName);
                    return;
                }
            }

            InputStream resource = plugin.getResource(fileName);
            if (resource != null) {
                Files.copy(resource, configFile.toPath());
                logger.info("Created config from resource:", fileName, "for", plugin.getName());
            } else {
                if (configFile.createNewFile()) {
                    logger.info("Created empty config:", fileName, "for", plugin.getName());
                } else {
                    logger.warn("Config file already exists:", fileName);
                }
            }
        } catch (IOException e) {
            logger.error(e, "Failed to create config:", fileName);
        }
    }

    private void loadDefaults(Plugin plugin, String fileName, FileConfiguration config) {
        InputStream resource = plugin.getResource(fileName);
        if (resource != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(resource)
            );
            config.setDefaults(defaults);
        }
    }
}