package org.extstudios.extCore.Internal.Database;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.extstudios.extCore.API.Database.DatabaseType;
import org.extstudios.extCore.API.LoggingService;
import org.extstudios.extCore.API.Task.TaskService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class YAMLDatabase extends AbstractDatabase {

    private final File file;
    private YamlConfiguration yaml;

    public YAMLDatabase(Plugin plugin, File file, LoggingService logger, TaskService taskService) {
        super(plugin, logger, taskService);
        this.file = file;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.YAML;
    }

    @Override
    public CompletableFuture<Void> connect() {
        return executeAsyncVoid(() -> {
            try {
                if (!file.exists()) {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    file.createNewFile();
                }

                yaml = YamlConfiguration.loadConfiguration(file);
                connected = true;
                logger.debug("Connected to YAML database:", file.getName());
            } catch (IOException e) {
                logger.error(e, "Failed to connect to YAML database");
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        return executeAsyncVoid(() -> {
            if (connected) {
                // Save before disconnecting
                try {
                    yaml.save(file);
                } catch (IOException e) {
                    logger.error(e, "Failed to save YAML database on disconnect");
                }
                yaml = null;
                connected = false;
                logger.debug("Disconnected from YAML database:", file.getName());
            }
        });
    }

    @Override
    public CompletableFuture<Void> save(String key, Object value) {
        return executeAsyncVoid(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            yaml.set(key, value);
            try {
                yaml.save(file);
            } catch (IOException e) {
                logger.error(e, "Failed to save YAML data");
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public <T> CompletableFuture<T> load(String key, Class<T> type) {
        return executeAsync(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            Object value = yaml.get(key);
            if (value == null) {
                return null;
            }
            return type.cast(value);
        });
    }

    @Override
    public CompletableFuture<Boolean> exists(String key) {
        return executeAsync(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            return yaml.contains(key);
        });
    }

    @Override
    public CompletableFuture<Void> delete(String key) {
        return executeAsyncVoid(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            yaml.set(key, null);
            try {
                yaml.save(file);
            } catch (IOException e) {
                logger.error(e, "Failed to save YAML after delete");
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<String>> getKeys() {
        return executeAsync(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            return new ArrayList<>(yaml.getKeys(false));
        });
    }
}
