package org.extstudios.extCore.Internal.Database;

import org.bukkit.plugin.Plugin;
import org.extstudios.extCore.API.Database.Database;
import org.extstudios.extCore.API.LoggingService;
import org.extstudios.extCore.API.Task.TaskService;
import org.extstudios.extCore.Internal.Platform;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractDatabase implements Database {

    protected final Plugin plugin;
    protected final LoggingService logger;
    protected final TaskService taskService;
    protected volatile boolean connected;

    public AbstractDatabase(Plugin plugin, LoggingService logger, TaskService taskService) {
        this.plugin = plugin;
        this.logger = logger;
        this.taskService = taskService;
        this.connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    protected <T> CompletableFuture<T> executeAsync(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();

        taskService.runAsync(plugin, () -> {
            try {
                future.complete(task.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
                logger.error(e, "Database operation failed");
            }
        });

        return future;
    }

    protected CompletableFuture<Void> executeAsyncVoid(Runnable task) {
        return executeAsync(() -> {
            task.run();
            return null;
        });
    }

    @Override
    public CompletableFuture<Integer> executeUpdate(String sql, Object... params) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("SQL operations not supported for " + getType())
        );
    }

    @Override
    public CompletableFuture<List<Map<String, Object>>> executeQuery(String sql, Object... params) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("SQL operations not supported for " + getType())
        );
    }

    @Override
    public CompletableFuture<Void> save(String key, Object value) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("Key-value operations not supported for " + getType())
        );
    }

    @Override
    public <T> CompletableFuture<T> load(String key, Class<T> type) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("Key-value operations not supported for " + getType())
        );
    }

    @Override
    public CompletableFuture<Boolean> exists(String key) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("Key-value operations not supported for " + getType())
        );
    }

    @Override
    public CompletableFuture<Void> delete(String key) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("Key-value operations not supported for " + getType())
        );
    }

    @Override
    public CompletableFuture<List<String>> getKeys() {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("Key-value operations not supported for " + getType())
        );
    }
}
