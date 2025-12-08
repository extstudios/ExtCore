package org.extstudios.extcore.internal.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;
import org.extstudios.extcore.api.database.DatabaseType;
import org.extstudios.extcore.api.database.SQLConfig;
import org.extstudios.extcore.api.LoggingService;
import org.extstudios.extcore.api.task.TaskService;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SQLDatabase extends AbstractDatabase {

    private final DatabaseType type;
    private final SQLConfig config;
    private HikariDataSource dataSource;

    public SQLDatabase(Plugin plugin, DatabaseType type, SQLConfig config,
                       LoggingService logger, TaskService taskService) {
        super(plugin, logger, taskService);
        this.type = type;
        this.config = config;
    }

    @Override
    public DatabaseType getType() {
        return type;
    }

    @Override
    public CompletableFuture<Void> connect() {
        return executeAsyncVoid(() -> {
            try {
                HikariConfig hikariConfig = new HikariConfig();

                switch (type) {
                    case MYSQL:
                        hikariConfig.setJdbcUrl(String.format(
                                "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC",
                                config.getHost(), config.getPort(), config.getDatabase()
                        ));
                        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
                        break;

                    case POSTGRESQL:
                        hikariConfig.setJdbcUrl(String.format(
                                "jdbc:postgresql://%s:%d/%s",
                                config.getHost(), config.getPort(), config.getDatabase()
                        ));
                        hikariConfig.setDriverClassName("org.postgresql.Driver");
                        break;

                    case SQLITE:
                        hikariConfig.setJdbcUrl("jdbc:sqlite:" + config.getDatabase());
                        hikariConfig.setDriverClassName("org.sqlite.JDBC");
                        break;

                    default:
                        throw new IllegalArgumentException("Unsupported SQL type: " + type);
                }

                if (type != DatabaseType.SQLITE) {
                    hikariConfig.setUsername(config.getUsername());
                    hikariConfig.setPassword(config.getPassword());
                }

                hikariConfig.setMinimumIdle(config.getMinimumIdle());
                hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
                hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
                hikariConfig.setIdleTimeout(config.getIdleTimeout());
                hikariConfig.setMaxLifetime(config.getMaxLifetime());
                hikariConfig.setAutoCommit(config.isAutoCommit());
                hikariConfig.setPoolName(plugin.getName() + "-" + type.name() + "-Pool");
                hikariConfig.setConnectionTestQuery("SELECT 1");

                dataSource = new HikariDataSource(hikariConfig);

                try (Connection conn = dataSource.getConnection()) {
                    if (conn == null || !conn.isValid(5)) {
                        throw new SQLException("Failed to validate connection");
                    }
                }

                connected = true;
                logger.info("Connected to", type.name(), "database with HikariCP pool");
                logger.debug("Pool size:", config.getMinimumIdle(), "-", config.getMaximumPoolSize());

            } catch (Exception e) {
                logger.error(e, "Failed to connect to SQL database");
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        return executeAsyncVoid(() -> {
            if (connected && dataSource != null) {
                dataSource.close();
                dataSource = null;
                connected = false;
                logger.info("Disconnected from", type.name(), "database");
            }
        });
    }

    @Override
    public Connection getConnection() {
        if (!connected || dataSource == null) {
            throw new IllegalStateException("Not connected to database");
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.error(e, "Failed to get connection from pool");
            return null;
        }
    }

    @Override
    public CompletableFuture<Integer> executeUpdate(String sql, Object... params) {
        return executeAsync(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                int rows = stmt.executeUpdate();
                logger.debug("Executed update:", sql, "- Rows affected:", rows);
                return rows;

            } catch (SQLException e) {
                logger.error(e, "Failed to execute update:", sql);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<Map<String, Object>>> executeQuery(String sql, Object... params) {
        return executeAsync(() -> {
            if (!connected) {
                throw new IllegalStateException("Not connected to database");
            }

            List<Map<String, Object>> results = new ArrayList<>();

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = meta.getColumnName(i);
                            Object value = rs.getObject(i);
                            row.put(columnName, value);
                        }
                        results.add(row);
                    }
                }

                logger.debug("Executed query:", sql, "- Results:", results.size());
                return results;

            } catch (SQLException e) {
                logger.error(e, "Failed to execute query:", sql);
                throw new RuntimeException(e);
            }
        });
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}