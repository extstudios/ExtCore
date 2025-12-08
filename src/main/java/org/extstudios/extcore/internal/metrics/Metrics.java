package org.extstudios.extcore.internal.metrics;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;


public class Metrics {

    private static final String METRICS_VERSION = "3.0.2";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, task -> {
        Thread thread = Executors.defaultThreadFactory().newThread(task);
        thread.setDaemon(true);
        return thread;
    });

    private final Plugin plugin;
    private final int pluginId;
    private final List<CustomChart> charts = new ArrayList<>();

    public Metrics(Plugin plugin, int pluginId) {
        this.plugin = plugin;
        this.pluginId = pluginId;

        // Check if bStats is enabled
        if (!isBStatsEnabled()) {
            return;
        }

        // Start submitting data
        startSubmitting();
    }

    public void addCustomChart(CustomChart chart) {
        charts.add(chart);
    }

    private void startSubmitting() {
        scheduler.scheduleAtFixedRate(this::submitData, 2, 30, TimeUnit.MINUTES);
    }

    private void submitData() {
        try {
            Map<String, Object> data = getServerData();
            Map<String, Object> pluginData = getPluginData();
            data.put("plugins", Collections.singletonList(pluginData));

            sendData(data);
        } catch (Exception ignored) {
            // Fail silently
        }
    }

    private Map<String, Object> getServerData() {
        Map<String, Object> data = new HashMap<>();

        data.put("serverUUID", getServerUUID());
        data.put("osName", System.getProperty("os.name"));
        data.put("osArch", System.getProperty("os.arch"));
        data.put("osVersion", System.getProperty("os.version"));
        data.put("coreCount", Runtime.getRuntime().availableProcessors());

        return data;
    }

    private Map<String, Object> getPluginData() {
        Map<String, Object> data = new HashMap<>();

        data.put("pluginName", plugin.getName());
        data.put("id", pluginId);
        data.put("pluginVersion", plugin.getPluginMeta().getVersion());
        data.put("customCharts", getChartData());

        return data;
    }

    private List<Map<String, Object>> getChartData() {
        List<Map<String, Object>> chartData = new ArrayList<>();

        for (CustomChart chart : charts) {
            Map<String, Object> data = chart.getRequestJsonObject();
            if (data != null) {
                chartData.add(data);
            }
        }

        return chartData;
    }

    private void sendData(Map<String, Object> data) throws Exception {
        String url = "https://bStats.org/api/v2/data/bukkit";
        HttpsURLConnection connection = (HttpsURLConnection) URI.create(url).toURL().openConnection();

        byte[] compressedData = compress(new com.google.gson.Gson().toJson(data));

        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Metrics-Service/" + METRICS_VERSION);

        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(compressedData);
        }

        connection.getInputStream().close();
    }

    private byte[] compress(String str) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
        }
        return outputStream.toByteArray();
    }

    private boolean isBStatsEnabled() {
        try {
            File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
            File configFile = new File(bStatsFolder, "config.yml");

            if (!configFile.exists()) {
                return true; // Default enabled
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            return config.getBoolean("enabled", true);
        } catch (Exception e) {
            return true;
        }
    }

    private String getServerUUID() {
        try {
            File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
            File configFile = new File(bStatsFolder, "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            if (config.contains("serverUuid")) {
                return config.getString("serverUuid");
            }

            String uuid = UUID.randomUUID().toString();
            config.set("serverUuid", uuid);
            config.set("enabled", true);
            config.save(configFile);

            return uuid;
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    public static abstract class CustomChart {
        private final String chartId;

        protected CustomChart(String chartId) {
            this.chartId = chartId;
        }

        public abstract Map<String, Object> getRequestJsonObject();

        protected Map<String, Object> createBaseChart() {
            Map<String, Object> chart = new HashMap<>();
            chart.put("chartId", chartId);
            return chart;
        }
    }

    public static class SimplePie extends CustomChart {
        private final Callable<String> callable;

        public SimplePie(String chartId, Callable<String> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        public Map<String, Object> getRequestJsonObject() {
            try {
                String value = callable.call();
                if (value == null || value.isEmpty()) {
                    return null;
                }

                Map<String, Object> chart = createBaseChart();
                Map<String, Object> data = new HashMap<>();
                data.put("value", value);
                chart.put("data", data);
                return chart;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class AdvancedPie extends CustomChart {
        private final Callable<Map<String, Integer>> callable;

        public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        public Map<String, Object> getRequestJsonObject() {
            try {
                Map<String, Integer> values = callable.call();
                if (values == null || values.isEmpty()) {
                    return null;
                }

                Map<String, Object> chart = createBaseChart();
                Map<String, Object> data = new HashMap<>();

                Map<String, Object> valuesMap = new HashMap<>(values);

                data.put("values", valuesMap);
                chart.put("data", data);
                return chart;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class SingleLineChart extends CustomChart {
        private final Callable<Integer> callable;

        public SingleLineChart(String chartId, Callable<Integer> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        public Map<String, Object> getRequestJsonObject() {
            try {
                Integer value = callable.call();
                if (value == null) {
                    return null;
                }

                Map<String, Object> chart = createBaseChart();
                Map<String, Object> data = new HashMap<>();
                data.put("value", value);
                chart.put("data", data);
                return chart;
            } catch (Exception e) {
                return null;
            }
        }
    }
}