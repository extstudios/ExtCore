package org.extstudios.extCore.Internal.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.extstudios.extCore.API.LoggingService;
import org.extstudios.extCore.API.PermissionService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionServiceImpl implements PermissionService, Listener {

    private final Plugin plugin;
    private final LoggingService logger;

    private final Map<UUID, Map<String, TemporaryPermission>> temporaryPermissions;
    private final Map<UUID, Map<String, Boolean>> permissionCache;

    private final Set<String> registeredDefaults;

    public PermissionServiceImpl(Plugin plugin, LoggingService logger) {
        this.plugin = plugin;
        this.logger = logger.withPrefix("[Permissions]");
        this.temporaryPermissions = new ConcurrentHashMap<>();
        this.permissionCache = new ConcurrentHashMap<>();
        this.registeredDefaults = ConcurrentHashMap.newKeySet();

        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkExpiredPermissions, 600L, 600L);

        logger.debug("PermissionService initialized");
    }

    @Override
    public boolean has(CommandSender sender, String permission) {
        if (sender == null || permission == null) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            return true;
        }

        UUID uuid = player.getUniqueId();
        Map<String, Boolean> cache = permissionCache.get(uuid);
        if (cache != null && cache.containsKey(permission)) {
            return cache.get(permission);
        }

        boolean result = checkPermission(player, permission);

        permissionCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(permission, result);

        return result;
    }

    @Override
    public boolean hasAny(CommandSender sender, String... permissions) {
        for (String permission : permissions) {
            if (has(sender, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAll(CommandSender sender, String... permissions) {
        for (String permission : permissions) {
            if (!has(sender, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getHighestLevel(CommandSender sender, String basePermission, int maxLevel) {
        for (int level = maxLevel; level >= 1; level--) {
            if (has(sender, basePermission + "." + level)) {
                return level;
            }
        }
        return 0;
    }

    @Override
    public boolean hasLevel(CommandSender sender, String basePermission, int requiredLevel) {
        int highestLevel = getHighestLevel(sender, basePermission, 100);
        return highestLevel >= requiredLevel;
    }

    @Override
    public Set<String> getPermissionsWithPrefix(CommandSender sender, String prefix) {
        if (!(sender instanceof Player player)) {
            return Set.of();
        }

        Set<String> matching = new HashSet<>();
        for (org.bukkit.permissions.PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (info.getPermission().startsWith(prefix) && info.getValue()) {
                matching.add(info.getPermission());
            }
        }

        return matching;
    }

    @Override
    public boolean isOp(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public boolean isPermissionPluginActive() {
        return Bukkit.getPluginManager().getPlugin("LuckPerms") != null
                || Bukkit.getPluginManager().getPlugin("PermissionsEx") != null
                || Bukkit.getPluginManager().getPlugin("GroupManager") != null
                || Bukkit.getPluginManager().getPlugin("bPermissions") != null;
    }

    @Override
    public void registerDefault(String permission, String description) {
        registerDefault(permission, description, PermissionDefault.OP);
    }

    @Override
    public void registerDefault(String permission, String description, PermissionDefault defaultValue) {
        if (registeredDefaults.contains(permission)) {
            logger.debug("Permission already registered:", permission);
            return;
        }

        try {
            Permission perm = new Permission(permission, description, defaultValue);
            Bukkit.getPluginManager().addPermission(perm);
            registeredDefaults.add(permission);
            logger.debug("Registered default permission:", permission, "with default:", defaultValue);
        } catch (IllegalArgumentException e) {
            // Permission already exists
            logger.debug("Permission already exists:", permission);
        }
    }

    @Override
    public void grantTemporary(Player player, String permission, long durationSeconds) {
        UUID uuid = player.getUniqueId();

        PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission(permission, true);

        long expiryTime = System.currentTimeMillis() + (durationSeconds * 1000);

        TemporaryPermission tempPerm = new TemporaryPermission(permission, expiryTime, attachment);
        temporaryPermissions.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(permission, tempPerm);

        permissionCache.remove(uuid);

        logger.debug("Granted temporary permission", permission, "to", player.getName(), "for", durationSeconds, "seconds");

        Bukkit.getScheduler().runTaskLater(plugin, () -> revokeTemporary(player, permission), durationSeconds * 20L);
    }

    @Override
    public void revokeTemporary(Player player, String permission) {
        UUID uuid = player.getUniqueId();
        Map<String, TemporaryPermission> perms = temporaryPermissions.get(uuid);

        if (perms == null) {
            return;
        }

        TemporaryPermission tempPerm = perms.remove(permission);
        if (tempPerm != null) {
            tempPerm.getAttachment().remove();

            permissionCache.remove(uuid);

            logger.debug("Revoked temporary permission", permission, "from", player.getName());
        }
    }

    @Override
    public boolean hasTemporary(Player player, String permission) {
        UUID uuid = player.getUniqueId();
        Map<String, TemporaryPermission> perms = temporaryPermissions.get(uuid);

        if (perms == null) {
            return false;
        }

        TemporaryPermission tempPerm = perms.get(permission);
        return tempPerm != null && !tempPerm.isExpired();
    }

    @Override
    public Set<String> getTemporaryPermissions(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, TemporaryPermission> perms = temporaryPermissions.get(uuid);

        if (perms == null) {
            return Set.of();
        }

        return new HashSet<>(perms.keySet());
    }

    @Override
    public void clearTemporary(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, TemporaryPermission> perms = temporaryPermissions.remove(uuid);

        if (perms != null) {
            for (TemporaryPermission tempPerm : perms.values()) {
                tempPerm.getAttachment().remove();
            }

            permissionCache.remove(uuid);

            logger.debug("Cleared all temporary permissions for", player.getName());
        }
    }

    @Override
    public long getTemporaryRemaining(Player player, String permission) {
        UUID uuid = player.getUniqueId();
        Map<String, TemporaryPermission> perms = temporaryPermissions.get(uuid);

        if (perms == null) {
            return 0;
        }

        TemporaryPermission tempPerm = perms.get(permission);
        return tempPerm != null ? tempPerm.getRemainingSeconds() : 0;
    }

    private boolean checkPermission(Player player, String permission) {

        if (player.hasPermission(permission)) {
            return true;
        }

        String[] parts = permission.split("\\.");
        StringBuilder wildcard = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            wildcard.append(parts[i]).append(".");
            if (player.hasPermission(wildcard + "*")) {
                return true;
            }
        }

        return player.hasPermission("*");
    }

    private void checkExpiredPermissions() {
        for (Map.Entry<UUID, Map<String, TemporaryPermission>> entry : temporaryPermissions.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, TemporaryPermission> perms = entry.getValue();

            List<String> toRemove = new ArrayList<>();

            for (Map.Entry<String, TemporaryPermission> permEntry : perms.entrySet()) {
                if (permEntry.getValue().isExpired()) {
                    toRemove.add(permEntry.getKey());
                }
            }

            for (String permission : toRemove) {
                TemporaryPermission tempPerm = perms.remove(permission);
                tempPerm.getAttachment().remove();
                logger.debug("Expired temporary permission:", permission, "for UUID:", uuid);
            }

            if (!toRemove.isEmpty()) {
                permissionCache.remove(uuid);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        Map<String, TemporaryPermission> perms = temporaryPermissions.remove(uuid);
        if (perms != null) {
            for (TemporaryPermission tempPerm : perms.values()) {
                tempPerm.getAttachment().remove();
            }
            logger.debug("Cleaned up temporary permissions for", event.getPlayer().getName());
        }

        permissionCache.remove(uuid);
    }

    public void shutdown() {
        logger.info("Shutting down PermissionService...");

        for (Map<String, TemporaryPermission> perms : temporaryPermissions.values()) {
            for (TemporaryPermission tempPerm : perms.values()) {
                tempPerm.getAttachment().remove();
            }
        }

        temporaryPermissions.clear();
        permissionCache.clear();

        logger.info("PermissionService shutdown complete");
    }
}

