package org.extstudios.extCore.Internal;

import org.extstudios.extCore.API.*;
import org.extstudios.extCore.API.Command.CommandService;
import org.extstudios.extCore.API.Core.CoreAPI;
import org.extstudios.extCore.API.Database.DatabaseService;
import org.extstudios.extCore.API.Inventory.InventoryService;
import org.extstudios.extCore.API.Task.TaskService;

public record CoreAPIImpl(String version, LoggingService loggingService, MessageService messageService,
                          ConfigService configService, TaskService taskService, CommandService commandService,
                          PlayerDataService playerDataService, PermissionService permissionService,
                          MetricsService metricsService, InventoryService inventoryService,
                          DatabaseService databaseService) implements CoreAPI {

}
