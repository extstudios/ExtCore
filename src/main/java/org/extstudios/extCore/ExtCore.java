package org.extstudios.extCore;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.extstudios.extCore.API.*;
import org.extstudios.extCore.API.Command.CommandService;
import org.extstudios.extCore.API.Core.CoreAPI;
import org.extstudios.extCore.API.Core.CoreProvider;
import org.extstudios.extCore.API.Database.DatabaseService;
import org.extstudios.extCore.API.Inventory.InventoryService;
import org.extstudios.extCore.API.Task.TaskService;
import org.extstudios.extCore.Internal.*;
import org.extstudios.extCore.Internal.Command.CommandServiceImpl;
import org.extstudios.extCore.Internal.Database.DatabaseServiceImpl;
import org.extstudios.extCore.Internal.Inventory.InventoryServiceImpl;
import org.extstudios.extCore.Internal.Metrics.MetricsServiceImpl;
import org.extstudios.extCore.Internal.Permission.PermissionServiceImpl;
import org.extstudios.extCore.Internal.Task.TaskServiceImpl;
import org.extstudios.extCore.Internal.Data.PlayerDataServiceImpl;

public final class ExtCore extends JavaPlugin {

    private LoggingService loggingService;
    private PlayerDataService playerDataService;
    private PermissionService permissionService;
    private MetricsService metricsService;
    private InventoryService inventoryService;
    private DatabaseService databaseService;

    @Override
    public void onEnable() {

        this.loggingService = new LoggingServiceImpl(getLogger());
        MessageService messageService = new MessageServiceImpl();
        ConfigService configService = new ConfigServiceImpl(loggingService);
        TaskService taskService = new TaskServiceImpl(loggingService);
        CommandService commandService = new CommandServiceImpl(loggingService, messageService);
        this.playerDataService = new PlayerDataServiceImpl(this, loggingService);
        this.permissionService = new PermissionServiceImpl(this, loggingService);
        this.metricsService = new MetricsServiceImpl(loggingService);
        this.inventoryService = new InventoryServiceImpl(this, loggingService, taskService);
        this.databaseService = new DatabaseServiceImpl(loggingService, taskService);

        CoreAPI coreAPI = new CoreAPIImpl(
                getPluginMeta().getVersion(),
                loggingService,
                messageService,
                configService,
                taskService,
                commandService,
                playerDataService,
                permissionService,
                metricsService,
                inventoryService,
                databaseService
        );

        CoreProvider.setInstance(coreAPI);

        getServer().getServicesManager().register(
                CoreAPI.class,
                coreAPI,
                this,
                ServicePriority.Normal
        );

        loggingService.header("EXTCore v" + getPluginMeta().getVersion());
        loggingService.info("Platform:", getServer().getName(), getServer().getVersion());
        loggingService.seperator();
    }

    @Override
    public void onDisable() {
        loggingService.info("EXTCore shutting down...");

        if (playerDataService instanceof PlayerDataServiceImpl) {
            ((PlayerDataServiceImpl) playerDataService).shutdown();
        }
        if (permissionService instanceof PermissionServiceImpl) {
            ((PermissionServiceImpl) permissionService).shutdown();
        }
        if (metricsService instanceof MetricsServiceImpl) {
            ((MetricsServiceImpl) metricsService).shutdown();
        }
        if (inventoryService instanceof InventoryServiceImpl) {
            ((InventoryServiceImpl) inventoryService).shutdown();
        }

        if (databaseService instanceof DatabaseServiceImpl) {
            ((DatabaseServiceImpl) databaseService).shutdown();
        }

        getServer().getServicesManager().unregisterAll(this);

        CoreProvider.clearInstance();

        loggingService.info("EXTCore disabled!");
    }
}
