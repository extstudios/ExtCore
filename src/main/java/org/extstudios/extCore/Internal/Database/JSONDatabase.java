package org.extstudios.extCore.Internal.Database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;
import org.extstudios.extCore.API.Database.DatabaseType;
import org.extstudios.extCore.API.LoggingService;
import org.extstudios.extCore.API.Task.TaskService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JSONDatabase extends AbstractDatabase {

    private final File file;
    private final Gson gson;
    private Map<String, Object> data;

    public JSONDatabase(Plugin plugin, File file, LoggingService logger, TaskService taskService) {
        super(plugin, logger, taskService);
        this.file = file;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.data = new HashMap<>();
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.JSON;
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
                    Files.write(file.toPath(), "{}".getBytes(StandardCharsets.UTF_8));
                }

                String content = Files.readString(file.toPath());
                if (content.trim().isEmpty()) {
                    content = "{}";
                }

                TypeToken<Map<String, Object>> token = new TypeToken<Map<String, Object>>() {};
                data = gson.fromJson(content, token.getType());
                if (data == null) {
                    data = new HashMap<>();
                }

                connected = true;
                logger.debug("Connected to JSON database:", file.getName());
            } catch (IOException e) {
                logger.error(e, "Failed to connect to JSON database");
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        return executeAsyncVoid(() -> {
            if (connected) {
                saveToFile();
                data.clear();
                connected = false;
                logger.debug("Disconnected from JSON database:", file.getName());
            }
        });
    }

    @Override
    public CompletableFuture<Void> save(String key, Object value) {
        return executeAsyncVoid(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            data.put(key, value);
            saveToFile();
        });
    }

    @Override
    public <T> CompletableFuture<T> load(String key, Class<T> type) {
        return executeAsync(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            Object value = data.get(key);
            if (value == null) {
                return null;
            }

            // Convert using Gson for proper type handling
            String json = gson.toJson(value);
            return gson.fromJson(json, type);
        });
    }

    @Override
    public CompletableFuture<Boolean> exists(String key) {
        return executeAsync(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            return data.containsKey(key);
        });
    }

    @Override
    public CompletableFuture<Void> delete(String key) {
        return executeAsyncVoid(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            data.remove(key);
            saveToFile();
        });
    }

    @Override
    public CompletableFuture<List<String>> getKeys() {
        return executeAsync(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }
            return new ArrayList<>(data.keySet());
        });
    }

    private void saveToFile() {
        try {
            String json = gson.toJson(data);
            Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error(e, "Failed to save JSON data");
            throw new RuntimeException(e);
        }
    }
}
