package org.extstudios.extcore.api.database;

import org.bukkit.plugin.Plugin;

import javax.xml.crypto.Data;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface DatabaseService {

    Database createYAML(Plugin plugin, String fileName);

    Database createYAML(File file);

    Database createJSON(Plugin plugin, String fileName);

    Database createJSON(File file);

    CompletableFuture<Database> createSQL(Plugin plugin, DatabaseType type, SQLConfig config);

    CompletableFuture<Database> createMySQL(Plugin plugin, SQLConfig config);

    CompletableFuture<Database> createPostgreSQL(Plugin plugin, SQLConfig config);

    CompletableFuture<Database> createSQLite(Plugin plugin, String fileName);

    CompletableFuture<Database> createSQLite(Plugin plugin, String fileName, SQLConfig config);

    int getActiveConnectionCount();

    CompletableFuture<Void> closeAll(Plugin plugin);

}