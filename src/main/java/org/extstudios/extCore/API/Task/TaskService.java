package org.extstudios.extCore.API.Task;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;


public interface TaskService {

    Task runAsync(Plugin plugin, Runnable task);

    Task runAsyncLater(Plugin plugin, Runnable task, long delayTicks);

    Task runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    Task runSync(Plugin plug, Runnable task);

    Task runSyncLater(Plugin plugin, Runnable task, long delayTicks);

    Task runSyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    Task runAtLocation(Plugin plugin, Location location, Runnable task);

    Task runAtLocationLater(Plugin plugin, Location location, Runnable task, long delayTicks);

    Task runAtLocationTimer(Plugin plugin, Location location, Runnable task, long delayTicks, long periodTicks);

    Task runAtEntity(Plugin plugin, Entity entity, Runnable task);

    Task runAtEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks);

    Task runAtEntityTimer(Plugin plugin, Entity entity, Runnable task, long delayTicks, long periodTicks);

    boolean isOnTickThread();

    boolean isFolia();

    String getPlatformDescription();
}
