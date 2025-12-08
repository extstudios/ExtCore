package org.extstudios.extcore.internal.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;
import org.extstudios.extcore.api.task.Task;

public class TaskImpl implements Task {

    private final Object owner;
    private final BukkitTask bukkitTask;
    private final ScheduledTask foliaTask;

    public TaskImpl(Object owner, BukkitTask bukkitTask) {
        this.owner = owner;
        this.bukkitTask = bukkitTask;
        this.foliaTask = null;
    }

    public TaskImpl(Object owner, ScheduledTask foliaTask) {
        this.owner = owner;
        this.bukkitTask = null;
        this.foliaTask = foliaTask;
    }

    @Override
    public void cancel() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        } else if (foliaTask != null) {
            foliaTask.cancel();
        }
    }

    @Override
    public boolean isCancelled() {
        if (bukkitTask != null) {
            return bukkitTask.isCancelled();
        } else if (foliaTask != null) {
            return foliaTask.isCancelled();
        }
        return true;
    }

    @Override
    public Object getOwner() {
        return owner;
    }
}