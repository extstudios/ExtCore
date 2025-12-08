# ExtCore

> Professional Minecraft plugin library with native Folia and Paper support

[![JitPack](https://jitpack.io/v/extstudios/ExtCore.svg)](https://jitpack.io/#extstudios/ExtCore)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Paper](https://img.shields.io/badge/Paper-1.21+-blue.svg)](https://papermc.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

ExtCore is a production-ready library providing **10 essential services** for Minecraft plugin development, with automatic platform optimization for both **Folia** (regional threading) and **Paper** (single-threaded) servers.

**No configuration needed** - ExtCore automatically detects your platform and optimizes accordingly.

---

## Features

### 10 Core Services

| Service | Description |
|---------|-------------|
| **LoggingService** | Advanced logging with automatic object formatting and debug toggle |
| **MessageService** | Adventure API with MiniMessage, action bars, titles, and sound effects |
| **ConfigService** | YAML management with hot-reload, multi-file support, and type-safe getters |
| **TaskService** | Folia/Paper unified scheduling with region/entity/location awareness |
| **CommandService** | Brigadier on Paper, Bukkit fallback on Folia with fluent builder API |
| **PlayerDataService** | Thread-safe per-player data caching with automatic cleanup |
| **PermissionService** | Wildcard support, temporary permissions, and level checking |
| **MetricsService** | bStats integration with custom charts |
| **InventoryService** | Folia-safe GUI builder with platform-specific optimization |
| **DatabaseService** | YAML, JSON, SQL (MySQL/PostgreSQL/SQLite) with HikariCP 7.0 pooling |

### Platform Optimization

- **Folia Support**: Regional threading, entity scheduling, async operations
- **Paper Support**: Single-threaded optimization, Brigadier commands, direct execution
- **Automatic Detection**: No configuration needed - works out of the box

### Database Features

- **HikariCP 7.0**: Professional connection pooling with configurable settings
- **Multiple Database Types**: YAML, JSON, MySQL, PostgreSQL, SQLite
- **Async Operations**: All operations return `CompletableFuture` for non-blocking execution
- **Thread-Safe**: Optimized for both Folia and Paper platforms
- **Connection Management**: Automatic cleanup and resource management

---

## Installation

### For Plugin Developers

ExtCore is designed to be **shaded into your plugin**. This means:
- ✅ No ExtCore.jar needed on the server
- ✅ Each plugin is self-contained
- ✅ No version conflicts between plugins
- ✅ Easy updates - just change version number

Add to your `build.gradle`:

```gradle
plugins {
    id 'java'
    id 'com.gradleup.shadow' version '9.2.2'
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
        mavenCentral()
        maven { url = 'https://repo.papermc.io/repository/maven-public/' }
        maven { url = 'https://jitpack.io' 
    }
}

dependencies {
    compileOnly 'io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT'
   
    implementation 'com.github.extstudios:ExtCore:v1.0.0'
    
    // Add database drivers if needed
    implementation 'com.mysql:mysql-connector-j:9.5.0'
    implementation 'org.xerial:sqlite-jdbc:3.51.1.0'
}

shadowJar {
    archiveClassifier.set('')
    
    // CRITICAL: Relocate to avoid conflicts with other plugins
    relocate 'org.extstudios.extcore', 'com.yourplugin.libs.extcore'
    
    // Also relocate database drivers
    relocate 'com.mysql', 'com.yourplugin.libs.mysql'
    relocate 'org.sqlite', 'com.yourplugin.libs.sqlite'
}

tasks.build.dependsOn(shadowJar)
```

**Result**: Single JAR file containing your plugin + ExtCore!

---

## Quick Start

### Basic Setup

```java
package com.example.myplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.extstudios.extcore.api.core.CoreAPI;
import org.extstudios.extcore.api.core.CoreProvider;

public class MyPlugin extends JavaPlugin {
    
    private CoreAPI api;
    
    @Override
    public void onEnable() {
        // Initialize ExtCore
        this.api = CoreProvider.initialize(this);
        
        // Use services
        api.getLoggingService().info("MyPlugin enabled!");
        api.getMessageService().broadcast("<green>Server is ready!</green>");
    }
    
    @Override
    public void onDisable() {
        // Clean up
        CoreProvider.clearInstance();
    }
}
```

---

## Service Documentation

### 1. LoggingService

Advanced logging with automatic object formatting.

```java
import org.extstudios.extcore.api.LoggingService;

LoggingService logger = api.getLoggingService();

// Basic logging
logger.info("Server started successfully");
logger.warn("Resource pack not found");
logger.error("Failed to connect to database");

// Logging with objects (automatic formatting)
logger.info("Player joined:", player.getName(), "from", player.getAddress());
logger.debug("Processing data:", complexObject);

// With exceptions
try {
    // risky operation
} catch (Exception e) {
    logger.error(e, "Operation failed");
}

// Debug mode (toggle at runtime)
logger.setDebug(true);
logger.debug("This only shows when debug is enabled");

// Module prefix
LoggingService moduleLogger = logger.withPrefix("[Economy]");
moduleLogger.info("Balance updated"); // Output: [Economy] Balance updated

// Headers and separators
logger.header("MyPlugin v1.0.0");
logger.seperator(); // Prints: ========================================
```

---

### 2. MessageService

Adventure API with MiniMessage for rich text formatting.

```java
import org.extstudios.extcore.api.MessageService;

MessageService messages = api.getMessageService();

// Send formatted messages
messages.send(player, "<green>Welcome <b>%s</b>!</green>", player.getName());
messages.send(player, "<rainbow>Rainbow text!</rainbow>");
messages.send(player, "<gradient:blue:purple>Gradient text</gradient>");

// Action bars
messages.actionBar(player, "<yellow>Coins: <gold>1000</gold>");

// Titles
messages.title(player, "<green>Level Up!", "<gray>You are now level 5");
messages.title(player, "<red>Warning!", "<gray>Low health", 10, 40, 10);

// Sounds
messages.sound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

// Broadcasting
messages.broadcast("<yellow>[Announcement] <white>Server restart in 5 minutes");
messages.broadcastTitle("<red>Server Restarting", "<gray>Please log out");

// Kick with reason
messages.kick(player, "<red>You have been kicked\n<gray>Reason: Spam");

// Parse MiniMessage without sending
Component component = messages.parse("<green>Formatted text</green>");
```

**MiniMessage Syntax:**
- `<green>text</green>` - Colors
- `<bold>text</bold>` - Bold
- `<italic>text</italic>` - Italic
- `<rainbow>text</rainbow>` - Rainbow effect
- `<gradient:red:blue>text</gradient>` - Gradients
- `<hover:show_text:'tooltip'>text</hover>` - Hover events
- `<click:run_command:'/help'>text</click>` - Click events

---

### 3. ConfigService

YAML configuration management with hot-reload support.

```java
import org.extstudios.extcore.api.ConfigService;

ConfigService config = api.getConfigService();

// Load or create config
config.loadOrCreate(this, "config.yml");

// Type-safe getters with defaults
String serverName = config.getString("server.name", "My Server");
int maxPlayers = config.getInt("server.max-players", 100);
boolean pvpEnabled = config.getBoolean("gameplay.pvp", true);
double taxRate = config.getDouble("economy.tax-rate", 0.05);
List<String> worlds = config.getStringList("worlds");

// Multi-file support
config.loadOrCreate(this, "messages.yml");
String welcomeMsg = config.getString("messages.yml", "welcome", "Welcome!");

// Setting values
config.set("server.name", "Updated Server");
config.set("economy.starting-balance", 1000);
config.save("config.yml");

// Sections
ConfigurationSection economy = config.getSection("economy");
if (economy != null) {
    double startingBalance = economy.getDouble("starting-balance");
    double taxRate = economy.getDouble("tax-rate");
}

// Keys
Set<String> keys = config.getKeys("economy", false);
keys.forEach(key -> logger.info("Key:", key));

// Check if exists
if (config.contains("database.mysql.host")) {
    // MySQL is configured
}

// Reload config
config.reload("config.yml");

// Get FileConfiguration directly
FileConfiguration rawConfig = config.getConfig(this, "config.yml");
```

**Example config.yml:**
```yaml
server:
  name: "My Server"
  max-players: 100

economy:
  starting-balance: 1000
  tax-rate: 0.05

database:
  type: "mysql"
  mysql:
    host: "localhost"
    port: 3306
    database: "minecraft"
```

---

### 4. TaskService

Platform-optimized task scheduling (Folia and Paper).

```java
import org.extstudios.extcore.api.task.TaskService;
import org.extstudios.extcore.api.task.Task;

TaskService tasks = api.getTaskService();

// Async task (background thread)
tasks.runAsync(this, () -> {
    // Heavy operation (database query, API call, etc.)
    logger.info("Async task completed");
});

// Sync task (main thread on Paper, appropriate thread on Folia)
tasks.run(this, () -> {
    // Server operations
    player.teleport(location);
});

// Delayed task (20 ticks = 1 second)
tasks.runLater(this, () -> {
    messages.send(player, "5 seconds have passed");
}, 100L); // 5 seconds

// Repeating task
Task timer = tasks.runTimer(this, task -> {
    messages.broadcast("<yellow>Reminder sent every minute");
}, 1200L, 1200L); // Start after 1 minute, repeat every minute

// Cancel task
timer.cancel();

// Entity-specific task (Folia: runs on entity's thread)
tasks.runAtEntity(this, player, () -> {
    player.setHealth(20.0);
    messages.send(player, "Health restored!");
});

// Location-specific task (Folia: runs on location's region thread)
tasks.runAtLocation(this, location, () -> {
    location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
});

// Advanced: Direct access to schedulers
if (Platform.isFolia()) {
    // Folia-specific code
} else {
    // Paper-specific code
}
```

**Platform Behavior:**
- **Paper**: All tasks run on main thread (except async)
- **Folia**: Tasks run on appropriate region/entity thread
- **ExtCore handles this automatically!**

---

### 5. CommandService

Fluent command builder with Brigadier on Paper.

```java
import org.extstudios.extcore.api.command.CommandService;
import org.extstudios.extcore.api.command.CommandFactory;

CommandService cmdService = api.getCommandService();

// Simple command
cmdService.register(this, 
    CommandFactory.create("heal")
        .description("Heal yourself")
        .permission("myplugin.heal")
        .playerOnly()
        .executor(ctx -> {
            ctx.getPlayer().setHealth(20.0);
            ctx.replySuccess("You have been healed!");
        })
);

// Command with arguments
cmdService.register(this,
    CommandFactory.create("give")
        .usage("/give <player> <item> [amount]")
        .permission("myplugin.give")
        .minArgs(2)
        .maxArgs(3)
        .executor(ctx -> {
            String targetName = ctx.getArg(0);
            String itemName = ctx.getArg(1);
            int amount = ctx.getArgAsInt(2, 1);
            
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                ctx.replyError("Player not found!");
                return;
            }
            
            Material material = Material.matchMaterial(itemName);
            if (material == null) {
                ctx.replyError("Invalid item!");
                return;
            }
            
            target.getInventory().addItem(new ItemStack(material, amount));
            ctx.replySuccess("Gave %s x%d %s", targetName, amount, itemName);
        })
);

// Command with subcommands
cmdService.register(this,
    CommandFactory.create("economy")
        .description("Economy commands")
        .alias("eco")
        .permission("myplugin.economy")
        .subCommand("balance")
            .description("Check balance")
            .executor(ctx -> {
                ctx.reply("Your balance: $1000");
            })
        .done()
        .subCommand("pay")
            .description("Pay another player")
            .minArgs(2)
            .executor(ctx -> {
                String target = ctx.getArg(0);
                int amount = ctx.getArgAsInt(1, 0);
                ctx.replySuccess("Paid $%d to %s", amount, target);
            })
        .done()
);

// Tab completion
cmdService.register(this,
    CommandFactory.create("warp")
        .tabCompleter((sender, args) -> {
            if (args.length == 1) {
                return List.of("spawn", "pvp", "shop");
            }
            return List.of();
        })
        .executor(ctx -> {
            String warp = ctx.getArg(0);
            ctx.reply("Warping to " + warp);
        })
);

// Console-only command
cmdService.register(this,
    CommandFactory.create("save")
        .consoleOnly()
        .executor(ctx -> {
            Bukkit.savePlayers();
            ctx.reply("Players saved");
        })
);

// Unregister command
cmdService.unregister(this, "mycommand");

// Unregister all plugin commands
cmdService.unregisterAll(this);
```

**CommandContext API:**
```java
ctx.getSender()           // Get command sender
ctx.getPlayer()           // Get player (null if console)
ctx.isPlayer()            // Check if sender is player
ctx.getArgs()             // Get all arguments
ctx.getArg(index)         // Get argument at index
ctx.getArgAsInt(index, def)     // Get argument as int
ctx.getArgAsDouble(index, def)  // Get argument as double
ctx.getArgAsBoolean(index, def) // Get argument as boolean
ctx.joinArgs(startIndex)        // Join args from index
ctx.reply(msg)            // Send message to sender
ctx.replySuccess(msg)     // Send success message
ctx.replyError(msg)       // Send error message
```

---

### 6. PlayerDataService

Thread-safe per-player data storage with automatic cleanup.

```java
import org.extstudios.extcore.api.PlayerDataService;

PlayerDataService playerData = api.getPlayerDataService();

// Store data
playerData.set(player, "coins", 1000);
playerData.set(player, "rank", "VIP");
playerData.set(player, "last-login", System.currentTimeMillis());

// Get data
int coins = playerData.get(player, "coins", Integer.class, 0);
String rank = playerData.get(player, "rank", String.class, "Default");
long lastLogin = playerData.get(player, "last-login", Long.class, 0L);

// Check if exists
if (playerData.has(player, "coins")) {
    // Player has coins data
}

// Remove data
playerData.remove(player, "temporary-flag");

// Get all keys for player
Set<String> keys = playerData.getKeys(player);

// Clear all data for player
playerData.clearAll(player);

// Example: Economy system
public class EconomyManager {
    private final PlayerDataService data;
    
    public void deposit(Player player, int amount) {
        int current = data.get(player, "coins", Integer.class, 0);
        data.set(player, "coins", current + amount);
    }
    
    public void withdraw(Player player, int amount) {
        int current = data.get(player, "coins", Integer.class, 0);
        if (current >= amount) {
            data.set(player, "coins", current - amount);
        }
    }
    
    public int getBalance(Player player) {
        return data.get(player, "coins", Integer.class, 0);
    }
}
```

**Features:**
- Automatic cleanup when player leaves
- Thread-safe for Folia
- Type-safe getters
- Per-player key-value storage

---

### 7. PermissionService

Advanced permission management with wildcard and temporary support.

```java
import org.extstudios.extcore.api.PermissionService;

PermissionService perms = api.getPermissionService();

// Check permission
if (perms.has(player, "myplugin.admin")) {
    // Player is admin
}

// Check multiple permissions (OR logic)
if (perms.hasAny(player, "myplugin.mod", "myplugin.admin")) {
    // Player has mod OR admin
}

// Check multiple permissions (AND logic)
if (perms.hasAll(player, "shop.use", "shop.admin")) {
    // Player has both permissions
}

// Wildcard support (built-in)
// Player with "myplugin.*" has all myplugin permissions
// Player with "*" has all permissions
if (perms.has(player, "myplugin.commands.teleport")) {
    // True if player has:
    // - myplugin.commands.teleport
    // - myplugin.commands.*
    // - myplugin.*
    // - *
}

// Temporary permissions (auto-expires)
perms.grantTemporary(player, "myplugin.fly", 300); // 5 minutes (300 seconds)
perms.grantTemporary(player, "myplugin.speed", 3600); // 1 hour

// Check if temporary permission is active
if (perms.hasTemporary(player, "myplugin.fly")) {
    // Permission is still active
}

// Remove temporary permission early
perms.revokeTemporary(player, "myplugin.fly");

// Get remaining time (in seconds)
long remaining = perms.getRemainingTime(player, "myplugin.fly");
if (remaining > 0) {
    messages.send(player, "Fly expires in " + remaining + " seconds");
}

// Permission levels (numeric permissions)
// If player has "myplugin.level.5", this returns 5
int level = perms.getHighestLevel(player, "myplugin.level", 100);

// Example: Limit homes by rank
int maxHomes = perms.getHighestLevel(player, "homes.limit", 50);
// Player with homes.limit.10 can have 10 homes
// Player with homes.limit.25 can have 25 homes
```

**Use Cases:**
- Temporary VIP rank trials
- Timed event permissions
- Cooldown-based permissions
- Level-based restrictions

---

### 8. MetricsService

bStats integration for plugin statistics.

```java
import org.extstudios.extcore.api.MetricsService;

MetricsService metrics = api.getMetricsService();

// Initialize with plugin ID from bStats
metrics.initialize(this, 12345); // Replace with your bStats ID

// Add custom charts
metrics.addCustomChart(this, new SimplePie("used_database", () -> {
    return config.getString("database.type"); // "MySQL", "SQLite", etc.
}));

metrics.addCustomChart(this, new AdvancedPie("player_ranks", () -> {
    Map<String, Integer> rankCounts = new HashMap<>();
    for (Player p : Bukkit.getOnlinePlayers()) {
        String rank = getRank(p);
        rankCounts.merge(rank, 1, Integer::sum);
    }
    return rankCounts;
}));

metrics.addCustomChart(this, new SingleLineChart("total_transactions", () -> {
    return getTodayTransactions();
}));
```

**Register at**: https://bstats.org/

---

### 9. InventoryService

Folia-safe GUI builder with click handlers.

```java
import org.extstudios.extcore.api.inventory.InventoryService;
import org.extstudios.extcore.api.inventory.InventoryBuilder;

InventoryService invService = api.getInventoryService();

// Simple menu
invService.create(this, "<green>Shop Menu", 3) // 3 rows
    .item(13, createShopItem(), event -> {
        Player clicker = (Player) event.getWhoClicked();
        messages.send(clicker, "You bought an item!");
        event.setCancelled(true);
    })
    .fillBorder(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
    .cancelAllClicks()
    .openFor(player);

// Complex example: Player selector
public void openPlayerSelector(Player viewer) {
    InventoryBuilder inv = invService.create(this, "<blue>Select Player", 6);
    
    int slot = 0;
    for (Player target : Bukkit.getOnlinePlayers()) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(target);
        meta.displayName(Component.text(target.getName()));
        skull.setItemMeta(meta);
        
        inv.item(slot++, skull, event -> {
            event.setCancelled(true);
            viewer.closeInventory();
            teleportTo(viewer, target);
        });
        
        if (slot >= 54) break;
    }
    
    inv.cancelAllClicks().openFor(viewer);
}

// Pagination example
public class PagedShop {
    private int page = 0;
    private List<ItemStack> items;
    
    public void open(Player player) {
        InventoryBuilder inv = invService.create(plugin, "Shop - Page " + (page + 1), 6);
        
        // Items for this page (45 per page)
        int start = page * 45;
        int end = Math.min(start + 45, items.size());
        
        for (int i = start; i < end; i++) {
            int slot = i - start;
            inv.item(slot, items.get(i), event -> {
                // Handle purchase
                event.setCancelled(true);
            });
        }
        
        // Navigation buttons
        if (page > 0) {
            inv.item(45, createButton("Previous"), event -> {
                page--;
                open(player);
                event.setCancelled(true);
            });
        }
        
        if (end < items.size()) {
            inv.item(53, createButton("Next"), event -> {
                page++;
                open(player);
                event.setCancelled(true);
            });
        }
        
        inv.cancelAllClicks().openFor(player);
    }
}

// Close handler
inv.onClose(event -> {
    Player player = (Player) event.getPlayer();
    messages.send(player, "Menu closed");
});
```

**Platform-Optimized:**
- Paper: Opens directly
- Folia: Opens on entity thread automatically

---

### 10. DatabaseService

Comprehensive database support with HikariCP connection pooling.

#### YAML Database

```java
import org.extstudios.extcore.api.database.DatabaseService;
import org.extstudios.extcore.api.database.Database;

DatabaseService dbService = api.getDatabaseService();

// Create YAML database
Database db = dbService.createYAML(this, "playerdata");

db.connect().thenRun(() -> {
    // Save data
    db.save("players." + uuid + ".name", "Steve");
    db.save("players." + uuid + ".coins", 1000);
    
    // Load data
    db.load("players." + uuid + ".coins", Integer.class).thenAccept(coins -> {
        logger.info("Player has", coins, "coins");
    });
    
    // Check if exists
    db.exists("players." + uuid).thenAccept(exists -> {
        if (exists) {
            // Player data exists
        }
    });
    
    // Delete
    db.delete("players." + uuid);
    
    // Get all keys
    db.getKeys().thenAccept(keys -> {
        keys.forEach(key -> logger.info("Key:", key));
    });
});
```

#### JSON Database

```java
// Create JSON database
Database jsonDb = dbService.createJSON(this, "settings");

jsonDb.connect().thenRun(() -> {
    // Save complex objects
    jsonDb.save("server.settings.pvp", true);
    jsonDb.save("server.settings.difficulty", "HARD");
    
    // Load with type safety
    jsonDb.load("server.settings.pvp", Boolean.class).thenAccept(pvp -> {
        logger.info("PVP enabled:", pvp);
    });
});
```

#### MySQL Database

```java
import org.extstudios.extcore.api.database.SQLConfig;

// Configure MySQL connection
SQLConfig mysqlConfig = SQLConfig.mysql(
    "localhost",    // host
    3306,          // port
    "minecraft",   // database name
    "root",        // username
    "password"     // password
);

// Optional: Customize pool settings
mysqlConfig
    .setMaximumPoolSize(20)        // Max connections
    .setMinimumIdle(5)             // Min idle connections
    .setConnectionTimeout(30000)   // 30 seconds
    .setIdleTimeout(600000)        // 10 minutes
    .setMaxLifetime(1800000);      // 30 minutes

// Create database
dbService.createMySQL(this, mysqlConfig).thenAccept(db -> {
    // Create tables
    db.executeUpdate(
        "CREATE TABLE IF NOT EXISTS players (" +
        "uuid VARCHAR(36) PRIMARY KEY, " +
        "name VARCHAR(16), " +
        "coins INT DEFAULT 0, " +
        "last_login BIGINT)"
    ).thenAccept(rows -> {
        logger.info("Table created");
    });
    
    // Insert data (with prepared statement - SQL injection safe)
    db.executeUpdate(
        "INSERT INTO players (uuid, name, coins) VALUES (?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE name = ?, coins = ?",
        uuid.toString(), playerName, 1000, playerName, 1000
    ).thenAccept(rows -> {
        logger.info("Inserted", rows, "row(s)");
    });
    
    // Query data
    db.executeQuery(
        "SELECT * FROM players WHERE uuid = ?",
        uuid.toString()
    ).thenAccept(results -> {
        if (!results.isEmpty()) {
            Map<String, Object> row = results.get(0);
            String name = (String) row.get("name");
            int coins = (int) row.get("coins");
            long lastLogin = (long) row.get("last_login");
            
            logger.info("Player:", name, "Coins:", coins);
        }
    });
    
    // Update data
    db.executeUpdate(
        "UPDATE players SET coins = ? WHERE uuid = ?",
        2000, uuid.toString()
    );
    
    // Delete data
    db.executeUpdate(
        "DELETE FROM players WHERE last_login < ?",
        System.currentTimeMillis() - 7776000000L // 90 days
    );
});
```

#### PostgreSQL Database

```java
SQLConfig pgConfig = SQLConfig.postgresql(
    "localhost",
    5432,
    "gamedb",
    "postgres",
    "password"
);

dbService.createPostgreSQL(this, pgConfig).thenAccept(db -> {
    // Use PostgreSQL-specific features
    db.executeUpdate(
        "CREATE TABLE IF NOT EXISTS players (" +
        "id SERIAL PRIMARY KEY, " +
        "uuid UUID NOT NULL UNIQUE, " +
        "name VARCHAR(16), " +
        "data JSONB)"
    );
});
```

#### SQLite Database

```java
// SQLite - file-based database (perfect for single servers)
dbService.createSQLite(this, "playerdata").thenAccept(db -> {
    db.executeUpdate(
        "CREATE TABLE IF NOT EXISTS players (" +
        "uuid TEXT PRIMARY KEY, " +
        "name TEXT, " +
        "coins INTEGER DEFAULT 0)"
    );
    
    // Same API as MySQL/PostgreSQL!
    db.executeUpdate("INSERT INTO players VALUES (?, ?, ?)", uuid, name, 1000);
    db.executeQuery("SELECT * FROM players WHERE uuid = ?", uuid);
});
```

#### Database Management

```java
// Get active connection count
int activeConnections = dbService.getActiveConnectionCount();
logger.info("Active database connections:", activeConnections);

// Close all databases (automatically done on plugin disable)
dbService.closeAll();

// Individual database control
if (db.isConnected()) {
    db.disconnect().thenRun(() -> {
        logger.info("Database disconnected");
    });
}
```

**Best Practices:**
- Always use prepared statements (prevents SQL injection)
- Use async operations (all methods return CompletableFuture)
- Close databases on plugin disable (ExtCore does this automatically)
- Use appropriate pool sizes for your server scale
- SQLite is great for single servers, MySQL/PostgreSQL for networks

---

## Building from Source

```bash
# Clone repository
git clone https://github.com/extstudios/ExtCore.git
cd ExtCore

# Build with Gradle
./gradlew clean shadowJar

# Output
# build/libs/ExtCore-1.0.10.jar
```

---

## Requirements

- **Java 21** or higher
- **Paper 1.21+** or **Folia 1.21+**
- **HikariCP 7.0** (included)
- **JDBC Drivers** (optional, add as needed):
    - MySQL: `com.mysql:mysql-connector-j:9.5.0`
    - PostgreSQL: `org.postgresql:postgresql:42.7.8`
    - SQLite: `org.xerial:sqlite-jdbc:3.51.1.0`

---

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## Reporting Issues

Found a bug? [Open an issue](https://github.com/extstudios/ExtCore/issues)

Please include:
- ExtCore version
- Server type (Paper/Folia) and version
- Steps to reproduce
- Error logs (if applicable)

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- [Paper](https://papermc.io/) - High-performance Minecraft server
- [Folia](https://github.com/PaperMC/Folia) - Regionized multithreading
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - Lightning-fast connection pooling
- [Adventure](https://docs.advntr.dev/) - Modern text API
- [bStats](https://bstats.org/) - Plugin metrics

---

## Links

- **JitPack**: [https://jitpack.io/#extstudios/ExtCore](https://jitpack.io/#extstudios/ExtCore)
- **Issues**: [GitHub Issues](https://github.com/extstudios/ExtCore/issues)

---

## Statistics

[![GitHub stars](https://img.shields.io/github/stars/extstudios/ExtCore?style=social)](https://github.com/extstudios/ExtCore/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/extstudios/ExtCore?style=social)](https://github.com/extstudios/ExtCore/network/members)
[![GitHub issues](https://img.shields.io/github/issues/extstudios/ExtCore)](https://github.com/extstudios/ExtCore/issues)

---

<p align="center">Made with ❤️ by ExtStudios</p>
<p align="center">⭐ Star this repository if you find it helpful!</p>