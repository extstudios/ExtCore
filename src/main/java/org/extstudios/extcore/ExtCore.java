package org.extstudios.extcore;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.extstudios.extcore.api.*;
import org.extstudios.extcore.api.command.CommandService;
import org.extstudios.extcore.api.core.CoreAPI;
import org.extstudios.extcore.api.core.CoreProvider;
import org.extstudios.extcore.api.database.DatabaseService;
import org.extstudios.extcore.api.inventory.InventoryService;
import org.extstudios.extcore.api.task.TaskService;
import org.extstudios.extcore.internal.*;
import org.extstudios.extcore.internal.command.CommandServiceImpl;
import org.extstudios.extcore.internal.database.DatabaseServiceImpl;
import org.extstudios.extcore.internal.inventory.InventoryServiceImpl;
import org.extstudios.extcore.internal.metrics.MetricsServiceImpl;
import org.extstudios.extcore.internal.permission.PermissionServiceImpl;
import org.extstudios.extcore.internal.task.TaskServiceImpl;
import org.extstudios.extcore.internal.data.PlayerDataServiceImpl;

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