package org.extstudios.extcore.api.database;

public class SQLConfig {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    // HikariCP pool settings
    private int minimumIdle = 2;
    private int maximumPoolSize = 10;
    private long connectionTimeout = 30000; // 30 seconds
    private long idleTimeout = 600000; // 10 minutes
    private long maxLifetime = 1800000; // 30 minutes
    private boolean autoCommit = true;

    public SQLConfig(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public static SQLConfig sqlite(String filePath) {
        return new SQLConfig("", 0, filePath, "", "");
    }

    public static SQLConfig mysql(String host, int port, String database, String username, String password) {
        return new SQLConfig(host, port, database, username, password);
    }

    public static SQLConfig postgresql(String host, int port, String database, String username, String password) {
        return new SQLConfig(host, port, database, username, password);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public SQLConfig setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
        return this;
    }

    public SQLConfig setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    public SQLConfig setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public SQLConfig setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public SQLConfig setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
        return this;
    }

    public SQLConfig setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }
}