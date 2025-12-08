package org.extstudios.extcore.internal.metrics;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.extstudios.extcore.api.LoggingService;
import org.extstudios.extcore.api.MetricsService;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsServiceImpl implements MetricsService {

    private final LoggingService logger;

    private final Map<Plugin, Metrics> metricsMap;

    private final boolean bStatsEnabled;

    public MetricsServiceImpl(LoggingService logger) {
        this.logger = logger.withPrefix("[Metrics]");
        this.metricsMap = new ConcurrentHashMap<>();
        this.bStatsEnabled = checkBStatsEnabled();

        if (bStatsEnabled) {
            logger.debug("bStats is enabled");
        } else {
            logger.debug("bStats is disabled in config");
        }
    }

    @Override
    public boolean enableMetrics(Plugin plugin, int pluginId) {
        if (!bStatsEnabled) {
            logger.debug("Cannot enable metrics - bStats is disabled globally");
            return false;
        }

        if (metricsMap.containsKey(plugin)) {
            logger.debug("Metrics already enabled for", plugin.getName());
            return true;
        }

        try {
            Metrics metrics = new Metrics(plugin, pluginId);
            metricsMap.put(plugin, metrics);
            logger.debug("Enabled metrics for", plugin.getName(), "with ID", pluginId);
            return true;
        } catch (Exception e) {
            logger.error(e, "Failed to enable metrics for", plugin.getName());
            return false;
        }
    }

    @Override
    public boolean isEnabled(Plugin plugin) {
        return metricsMap.containsKey(plugin);
    }

    @Override
    public void disableMetrics(Plugin plugin) {
        Metrics metrics = metricsMap.remove(plugin);
        if (metrics != null) {
            logger.debug("Disabled metrics for", plugin.getName());
        }
    }

    @Override
    public void addCustomChart(Plugin plugin, String chartId, Callable<String> valueCallable) {
        addSimplePie(plugin, chartId, valueCallable);
    }

    @Override
    public void addSimplePie(Plugin plugin, String chartId, Callable<String> valueCallable) {
        Metrics metrics = metricsMap.get(plugin);
        if (metrics == null) {
            logger.warn("Cannot add chart - metrics not enabled for", plugin.getName());
            return;
        }

        metrics.addCustomChart(new Metrics.SimplePie(chartId, valueCallable));
        logger.debug("Added SimplePie chart", chartId, "for", plugin.getName());
    }

    @Override
    public void addAdvancedPie(Plugin plugin, String chartId, Callable<Map<String, Integer>> valuesCallable) {
        Metrics metrics = metricsMap.get(plugin);
        if (metrics == null) {
            logger.warn("Cannot add chart - metrics not enabled for", plugin.getName());
            return;
        }

        metrics.addCustomChart(new Metrics.AdvancedPie(chartId, valuesCallable));
        logger.debug("Added AdvancedPie chart", chartId, "for", plugin.getName());
    }

    @Override
    public void addDrilldownPie(Plugin plugin, String chartId, Callable<Map<String, Map<String, Integer>>> valuesCallable) {
        Metrics metrics = metricsMap.get(plugin);
        if (metrics == null) {
            logger.warn("Cannot add chart - metrics not enabled for", plugin.getName());
            return;
        }

        Callable<Map<String, Integer>> adapter = () -> {
            Map<String, Map<String, Integer>> nested = valuesCallable.call();
            Map<String, Integer> flattened = new java.util.HashMap<>();
            if (nested != null) {
                for (Map.Entry<String, Map<String, Integer>> entry : nested.entrySet()) {
                    String parentKey = entry.getKey();
                    for (Map.Entry<String, Integer> subEntry : entry.getValue().entrySet()) {
                        flattened.put(parentKey + " - " + subEntry.getKey(), subEntry.getValue());
                    }
                }
            }
            return flattened;
        };

        metrics.addCustomChart(new Metrics.AdvancedPie(chartId, adapter));
        logger.debug("Added Drill down Pie chart", chartId, "for", plugin.getName());
    }

    @Override
    public void addSingleLineChart(Plugin plugin, String chartId, Callable<Integer> valueCallable) {
        Metrics metrics = metricsMap.get(plugin);
        if (metrics == null) {
            logger.warn("Cannot add chart - metrics not enabled for", plugin.getName());
            return;
        }

        metrics.addCustomChart(new Metrics.SingleLineChart(chartId, valueCallable));
        logger.debug("Added SingleLineChart", chartId, "for", plugin.getName());
    }

    @Override
    public void addMultiLineChart(Plugin plugin, String chartId, Callable<Map<String, Integer>> valuesCallable) {
        Metrics metrics = metricsMap.get(plugin);
        if (metrics == null) {
            logger.warn("Cannot add chart - metrics not enabled for", plugin.getName());
            return;
        }

        metrics.addCustomChart(new Metrics.AdvancedPie(chartId, valuesCallable));
        logger.debug("Added MultiLineChart", chartId, "for", plugin.getName());
    }

    @Override
    public void addSimpleBarChart(Plugin plugin, String chartId, Callable<Map<String, Integer>> valuesCallable) {
        Metrics metrics = metricsMap.get(plugin);
        if (metrics == null) {
            logger.warn("Cannot add chart - metrics not enabled for", plugin.getName());
            return;
        }

        metrics.addCustomChart(new Metrics.AdvancedPie(chartId, valuesCallable));
        logger.debug("Added SimpleBarChart", chartId, "for", plugin.getName());
    }

    @Override
    public void addAdvancedBarChart(Plugin plugin, String chartId, Callable<Map<String, int[]>> valuesCallable) {
        Metrics metrics = metricsMap.get(plugin);
        if (metrics == null) {
            logger.warn("Cannot add chart - metrics not enabled for", plugin.getName());
            return;
        }

        Callable<Map<String, Integer>> adapter = () -> {
            Map<String, int[]> arrays = valuesCallable.call();
            Map<String, Integer> sums = new java.util.HashMap<>();
            if (arrays != null) {
                for (Map.Entry<String, int[]> entry : arrays.entrySet()) {
                    int sum = 0;
                    for (int value : entry.getValue()) {
                        sum += value;
                    }
                    sums.put(entry.getKey(), sum);
                }
            }
            return sums;
        };

        metrics.addCustomChart(new Metrics.AdvancedPie(chartId, adapter));
        logger.debug("Added AdvancedBarChart", chartId, "for", plugin.getName());
    }

    @Override
    public int getTrackedPluginCount() {
        return metricsMap.size();
    }

    @Override
    public boolean isBStatsEnabled() {
        return bStatsEnabled;
    }

    private boolean checkBStatsEnabled() {
        try {
            // Check bStats config folder (one level up from any plugin)
            File bStatsFolder = new File("plugins/bStats");
            File configFile = new File(bStatsFolder, "config.yml");

            if (!configFile.exists()) {
                return true; // Default enabled
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            return config.getBoolean("enabled", true);
        } catch (Exception e) {
            return true; // Default enabled if error
        }
    }

    public void shutdown() {
        logger.info("Shutting down MetricsService...");
        metricsMap.clear();
        logger.info("MetricsService shutdown complete");
    }
}