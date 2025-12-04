package org.extstudios.extCore.Internal;

import org.bukkit.Bukkit;

public class Platform {

    private static final boolean IS_FOLIA;
    private static final String PLATFORM_NAME;
    private static final String PLATFORM_VERSION;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {

        }

        IS_FOLIA = folia;
        PLATFORM_NAME = Bukkit.getName();
        PLATFORM_VERSION = Bukkit.getVersion();
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static boolean isPaper() {
        return !IS_FOLIA;
    }

    public static String getPlatformName() {
        return IS_FOLIA ? "Folia" : PLATFORM_NAME;
    }

    public static String getPlatformVersion() {
        return PLATFORM_VERSION;
    }

    public static String getDescription() {
        if (IS_FOLIA) {
            return "Folia (Regional Threading)";
        } else {
            return PLATFORM_NAME + " (Single Thread)";
        }
    }
}
