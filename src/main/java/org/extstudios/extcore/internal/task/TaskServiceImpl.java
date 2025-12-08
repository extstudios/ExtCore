package org.extstudios.extcore.internal.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.extstudios.extcore.api.LoggingService;
import org.extstudios.extcore.api.task.Task;
import org.extstudios.extcore.api.task.TaskService;
import org.extstudios.extcore.internal.Platform;

import java.util.concurrent.TimeUnit;

public class TaskServiceImpl implements TaskService {

    private final boolean isFolia;

    public TaskServiceImpl(LoggingService logger) {
        LoggingService logger1 = logger.withPrefix("[Tasks]");
        this.isFolia = Platform.isFolia();

        logger1.info("Task Scheduler initialized for:", Platform.getDescription());
    }

    @Override
    public Task runAsync(Plugin plugin, Runnable task) {
        if (isFolia) {
            return wrapFoliaTask(plugin, Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run()));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
        }
    }

    @Override
    public Task runAsyncLater(Plugin plugin, Runnable task, long delayTicks) {
        long delayMs = delayTicks * 50L;

        if (isFolia) {
            return wrapFoliaTask(plugin, Bukkit.getAsyncScheduler().runDelayed(
                    plugin,
                    scheduledTask -> task.run(),
                    delayMs,
                    TimeUnit.MICROSECONDS
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
        }
    }

    @Override
    public Task runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        long delayMs = delayTicks * 50L;
        long periodMs = periodTicks * 50L;

        if (isFolia) {
            return wrapFoliaTask(plugin, Bukkit.getAsyncScheduler().runAtFixedRate(
                    plugin,
                    scheduledTask -> task.run(),
                    delayMs,
                    periodMs,
                    TimeUnit.MILLISECONDS
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
        }
    }

    @Override
    public Task runSync(Plugin plugin, Runnable task) {
        if (isFolia) {
            return wrapFoliaTask(plugin, Bukkit.getGlobalRegionScheduler().run( plugin, scheduledTask -> task.run()));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTask(plugin, task));
        }
    }

    @Override
    public Task runSyncLater(Plugin plugin, Runnable task, long delayTicks) {
        if (isFolia) {
            return wrapFoliaTask(plugin, Bukkit.getGlobalRegionScheduler().runDelayed(
                    plugin,
                    scheduledTask -> task.run(),
                    delayTicks
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTask(plugin, task));
        }
    }

    @Override
    public Task runSyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (isFolia) {
            return wrapFoliaTask(plugin, Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                    plugin,
                    scheduledTask -> task.run(),
                    delayTicks,
                    periodTicks
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    @Override
    public Task runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (isFolia) {
            return wrapFoliaTask(plugin, Bukkit.getRegionScheduler().run(
                    plugin,
                    location,
                    scheduledTask -> task.run()
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTask(plugin, task));
        }
    }
    @Override
    public Task runAtLocationLater(Plugin plugin, Location location, Runnable task, long delayTicks) {
        if (isFolia) {
            return wrapFoliaTask(plugin, Bukkit.getRegionScheduler().runDelayed(
                    plugin,
                    location,
                    scheduledTask -> task.run(),
                    delayTicks
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
        }
    }

    @Override
    public Task runAtLocationTimer(Plugin plugin, Location location, Runnable task, long delayTicks, long periodTicks) {
        if (isFolia) {
            return wrapFoliaTask(plugin, Bukkit.getRegionScheduler().runAtFixedRate(
                    plugin,
                    location,
                    scheduledTask -> task.run(),
                    delayTicks,
                    periodTicks
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    @Override
    public Task runAtEntity(Plugin plugin, Entity entity, Runnable task) {
        if (isFolia) {
            return wrapFoliaTask(plugin, entity.getScheduler().run(
                    plugin,
                    scheduledTask -> task.run(),
                    null
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTask(plugin, task));
        }
    }

    @Override
    public Task runAtEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (isFolia) {
            return wrapFoliaTask(plugin, entity.getScheduler().runDelayed(
                    plugin,
                    scheduledTask -> task.run(),
                    null,
                    delayTicks
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
        }
    }

    @Override
    public Task runAtEntityTimer(Plugin plugin, Entity entity, Runnable task, long delayTicks, long periodTicks) {
        if (isFolia) {
            return wrapFoliaTask(plugin, entity.getScheduler().runAtFixedRate(
                    plugin,
                    scheduledTask -> task.run(),
                    null,
                    delayTicks,
                    periodTicks
            ));
        } else {
            return wrapBukkitTask(plugin, Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    @Override
    public boolean isOnTickThread() {
        if (isFolia) {
            return Bukkit.isOwnedByCurrentRegion((Location) null);
        } else {
            return Bukkit.isPrimaryThread();
        }
    }

    @Override
    public boolean isFolia() {
        return isFolia;
    }

    @Override
    public String getPlatformDescription() {
        return Platform.getDescription();
    }

    private Task wrapBukkitTask(Plugin plugin, BukkitTask bukkitTask) {
        return new TaskImpl(plugin, bukkitTask);
    }

    private Task wrapFoliaTask(Plugin plugin, ScheduledTask foliaTask) {
        return new TaskImpl(plugin, foliaTask);
    }
}