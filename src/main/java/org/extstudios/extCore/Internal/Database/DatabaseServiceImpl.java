package org.extstudios.extCore.Internal.Database;

import org.bukkit.plugin.Plugin;
import org.extstudios.extCore.API.Database.Database;
import org.extstudios.extCore.API.Database.DatabaseService;
import org.extstudios.extCore.API.Database.DatabaseType;
import org.extstudios.extCore.API.Database.SQLConfig;
import org.extstudios.extCore.API.LoggingService;
import org.extstudios.extCore.API.Task.TaskService;
import org.extstudios.extCore.Internal.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseServiceImpl implements DatabaseService {

    private final LoggingService logger;
    private final TaskService taskService;
    private final boolean isFolia;

    private final Map<Plugin, List<Database>> databases;

    public DatabaseServiceImpl(LoggingService logger, TaskService taskService) {
        this.logger = logger.withPrefix("[Database]");
        this.taskService = taskService;
        this.isFolia = Platform.isFolia();
        this.databases = new ConcurrentHashMap<>();

        if (isFolia) {
            logger.debug("DatabaseService initialized (Folia mode - regional threading)");
        } else {
            logger.debug("DatabaseService initialized (Paper mode - single threading)");
        }
    }

    @Override
    public Database createYAML(Plugin plugin, String fileName) {
        File file = new File(plugin.getDataFolder(), fileName + ".yml");
        return createYAML(file);
    }

    @Override
    public Database createYAML(File file) {
        YAMLDatabase database = new YAMLDatabase(getPluginFromFile(file), file, logger, taskService);
        registerDatabase(getPluginFromFile(file), database);
        logger.debug("Created YAML database:", file.getName());
        return database;
    }

    @Override
    public Database createJSON(Plugin plugin, String fileName) {
        File file = new File(plugin.getDataFolder(), fileName + ".json");
        return createJSON(file);
    }

    @Override
    public Database createJSON(File file) {
        JSONDatabase database = new JSONDatabase(getPluginFromFile(file), file, logger, taskService);
        registerDatabase(getPluginFromFile(file), database);
        logger.debug("Created JSON database:", file.getName());
        return database;
    }

    @Override
    public CompletableFuture<Database> createSQL(Plugin plugin, DatabaseType type, SQLConfig config) {
        if (type != DatabaseType.MYSQL && type != DatabaseType.POSTGRESQL && type != DatabaseType.SQLITE) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Invalid SQL type: " + type)
            );
        }

        SQLDatabase database = new SQLDatabase(plugin, type, config, logger, taskService);
        registerDatabase(plugin, database);

        return database.connect().thenApply(v -> {
            logger.info("Created and connected to", type.name(), "database for", plugin.getName());
            return database;
        });
    }

    @Override
    public CompletableFuture<Database> createMySQL(Plugin plugin, SQLConfig config) {
        return createSQL(plugin, DatabaseType.MYSQL, config);
    }

    @Override
    public CompletableFuture<Database> createPostgreSQL(Plugin plugin, SQLConfig config) {
        return createSQL(plugin, DatabaseType.POSTGRESQL, config);
    }

    @Override
    public CompletableFuture<Database> createSQLite(Plugin plugin, String fileName) {
        File dbFile = new File(plugin.getDataFolder(), fileName + ".db");
        SQLConfig config = SQLConfig.sqlite(dbFile.getAbsolutePath());

        config.setMinimumIdle(1);
        config.setMaximumPoolSize(1);

        return createSQL(plugin, DatabaseType.SQLITE, config);
    }

    @Override
    public CompletableFuture<Database> createSQLite(Plugin plugin, String fileName, SQLConfig config) {
        File dbFile = new File(plugin.getDataFolder(), fileName + ".db");

        SQLConfig customConfig = SQLConfig.sqlite(dbFile.getAbsolutePath())
                .setMinimumIdle(config.getMinimumIdle())
                .setMaximumPoolSize(config.getMaximumPoolSize())
                .setConnectionTimeout(config.getConnectionTimeout())
                .setIdleTimeout(config.getIdleTimeout())
                .setMaxLifetime(config.getMaxLifetime())
                .setAutoCommit(config.isAutoCommit());

        return createSQL(plugin, DatabaseType.SQLITE, customConfig);
    }

    @Override
    public int getActiveConnectionCount() {
        return databases.values().stream()
                .flatMap(List::stream)
                .mapToInt(db -> db.isConnected() ? 1 : 0)
                .sum();
    }

    @Override
    public CompletableFuture<Void> closeAll(Plugin plugin) {
        List<Database> pluginDatabases = databases.remove(plugin);
        if (pluginDatabases == null || pluginDatabases.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<Void>> closeFutures = new ArrayList<>();
        for (Database database : pluginDatabases) {
            closeFutures.add(database.disconnect());
        }

        return CompletableFuture.allOf(closeFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    logger.info("Closed", pluginDatabases.size(), "database(s) for", plugin.getName());
                });
    }

    private void registerDatabase(Plugin plugin, Database database) {
        databases.computeIfAbsent(plugin, k -> new ArrayList<>()).add(database);
    }

    private Plugin getPluginFromFile(File file) {
        return null;
    }

    public void shutdown() {
        logger.info("Shutting down DatabaseService...");

        int totalDatabases = 0;
        List<CompletableFuture<Void>> closeFutures = new ArrayList<>();

        for (Map.Entry<Plugin, List<Database>> entry : databases.entrySet()) {
            for (Database database : entry.getValue()) {
                if (database.isConnected()) {
                    closeFutures.add(database.disconnect());
                    totalDatabases++;
                }
            }
        }

        CompletableFuture.allOf(closeFutures.toArray(new CompletableFuture[0]))
                .orTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .join();

        databases.clear();

        logger.info("DatabaseService shutdown complete - Closed", totalDatabases, "database(s)");
    }
}
