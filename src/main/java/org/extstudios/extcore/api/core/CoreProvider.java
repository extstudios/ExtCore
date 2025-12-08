package org.extstudios.extcore.api.core;

public final class CoreProvider {

    private static CoreAPI instance;

    private CoreProvider() {

    }

    public static CoreAPI get() {
        if (instance == null) {
            throw new IllegalStateException("EXTCore has not been initialized yet!");
        }
        return instance;
    }

    public static void setInstance(CoreAPI api) {
        if (instance != null) {
            throw new IllegalStateException("CoreAPI instance has already been set!");
        }
        instance = api;
    }

    public static void clearInstance() {
        instance = null;
    }
}