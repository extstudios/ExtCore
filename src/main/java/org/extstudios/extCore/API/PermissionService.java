package org.extstudios.extCore.API;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Set;

public interface PermissionService {

    boolean has(CommandSender sender, String permission);

    boolean hasAny(CommandSender sender, String... permissions);

    boolean hasAll(CommandSender sender, String... permissions);

    int getHighestLevel(CommandSender sender, String basePermission, int maxLevel);

    boolean hasLevel(CommandSender sender, String basePermission, int requiredLevel);

    Set<String> getPermissionsWithPrefix(CommandSender sender, String prefix);

    boolean isOp(CommandSender sender);

    boolean isPermissionPluginActive();

    void registerDefault(String permission, String description);

    void registerDefault(String permission, String description, PermissionDefault defaultValue);

    void grantTemporary(Player player, String permission, long durationSeconds);

    void revokeTemporary(Player player, String permission);

    boolean hasTemporary(Player player, String permission);

    Set<String> getTemporaryPermissions(Player player);

    void clearTemporary(Player player);

    long getTemporaryRemaining(Player player, String permission);
}
