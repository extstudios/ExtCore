package org.extstudios.extCore.API.Task;

public interface Task {

    void cancel();

    boolean isCancelled();

    Object getOwner();
}
