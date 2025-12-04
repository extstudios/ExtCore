package org.extstudios.extCore.Internal.Permission;

import org.bukkit.permissions.PermissionAttachment;

public class TemporaryPermission {

    private final String permission;
    private final long expiryTime;
    private final PermissionAttachment attachment;

    public TemporaryPermission(String permission, long expiryTime, PermissionAttachment attachment) {
        this.permission = permission;
        this.expiryTime = expiryTime;
        this.attachment = attachment;
    }

    public String getPermission() {
        return permission;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public PermissionAttachment getAttachment() {
        return attachment;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiryTime;
    }

    public long getRemainingSeconds() {
        long remaining = expiryTime - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }
}
