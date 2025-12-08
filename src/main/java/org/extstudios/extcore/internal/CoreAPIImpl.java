package org.extstudios.extcore.internal;

import org.extstudios.extcore.api.*;
import org.extstudios.extcore.api.command.CommandService;
import org.extstudios.extcore.api.core.CoreAPI;
import org.extstudios.extcore.api.database.DatabaseService;
import org.extstudios.extcore.api.inventory.InventoryService;
import org.extstudios.extcore.api.task.TaskService;

public record CoreAPIImpl(String version, LoggingService loggingService, MessageService messageService,
                          ConfigService configService, TaskService taskService, CommandService commandService,
                          PlayerDataService playerDataService, PermissionService permissionService,
                          MetricsService metricsService, InventoryService inventoryService,
                          DatabaseService databaseService) implements CoreAPI {

}