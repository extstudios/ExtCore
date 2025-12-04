package org.extstudios.extCore.API;

import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.Callable;

public interface MetricsService {

    boolean enableMetrics(Plugin plugin, int pluginId);

    boolean isEnabled(Plugin plugin);

    void disableMetrics(Plugin plugin);

    void addCustomChart(Plugin plugin, String chartId, Callable<String> valueCallable);

    void addSimplePie(Plugin plugin, String chartId, Callable<String> valueCallable);

    void addAdvancedPie(Plugin plugin, String chartId, Callable<Map<String, Integer>> valuesCallable);

    void addDrilldownPie(Plugin plugin, String chartId, Callable<Map<String, Map<String, Integer>>> valuesCallable);

    void addSingleLineChart(Plugin plugin, String chartId, Callable<Integer> valueCallable);

    void addMultiLineChart(Plugin plugin, String chartId, Callable<Map<String, Integer>> valuesCallable);

    void addSimpleBarChart(Plugin plugin, String chartId, Callable<Map<String, Integer>> valuesCallable);

    void addAdvancedBarChart(Plugin plugin, String chartId, Callable<Map<String, int[]>> valuesCallable);

    int getTrackedPluginCount();

    boolean isBStatsEnabled();
}
