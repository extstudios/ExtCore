package org.extstudios.extcore.api.task;

public interface Task {

    void cancel();

    boolean isCancelled();

    Object getOwner();
}