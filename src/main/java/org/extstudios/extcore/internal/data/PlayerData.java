package org.extstudios.extcore.internal.data;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    private final UUID uuid;
    private final Map<String, Object> data;
    private volatile boolean dirty;
    private volatile long lastAccessed;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.data = new ConcurrentHashMap<>();
        this.dirty = false;
        this.lastAccessed = System.currentTimeMillis();
    }

    public void set(String key, Object value) {
        data.put(key, value);
        markDirty();
        updateAccess();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        updateAccess();
        Object value = data.get(key);

        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        if (type == String.class) {
            return (T) String.valueOf(value);
        }

        switch (value) {
            case Number number when type == Integer.class -> {
                return (T) Integer.valueOf(number.intValue());
            }
            case Number number when type == Double.class -> {
                return (T) Double.valueOf(number.doubleValue());
            }
            case Number number when type == Long.class -> {
                return (T) Long.valueOf(number.longValue());
            }
            default -> {
            }
        }

        if (type == Boolean.class) {
            if (value instanceof Boolean) {
                return (T) value;
            }
            return (T) Boolean.valueOf(String.valueOf(value));
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        updateAccess();
        Object value = data.get(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public boolean has(String key) {
        updateAccess();
        return data.containsKey(key);
    }

    public void remove(String key) {
        data.remove(key);
        markDirty();
        updateAccess();
    }

    public void clear() {
        data.clear();
        markDirty();
        updateAccess();
    }

    public Map<String, Object> getAll() {
        updateAccess();
        return Collections.unmodifiableMap(data);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        this.dirty = false;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    private void updateAccess() {
        this.lastAccessed = System.currentTimeMillis();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public int size() {
        return data.size();
    }
}