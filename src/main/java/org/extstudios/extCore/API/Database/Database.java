package org.extstudios.extCore.API.Database;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Database {

    DatabaseType getType();

    CompletableFuture<Void> connect();

    CompletableFuture<Void> disconnect();

    boolean isConnected();

    Connection getConnection();

    CompletableFuture<Integer> executeUpdate(String sql, Object... params);

    CompletableFuture<List<Map<String, Object>>> executeQuery(String sql, Object... params);

    CompletableFuture<Void> save(String key, Object value);

    <T> CompletableFuture<T> load(String key, Class<T> type);

    CompletableFuture<Boolean> exists(String key);

    CompletableFuture<Void> delete(String key);

    CompletableFuture<List<String>> getKeys();
}
