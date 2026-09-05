package com.Shinzrtx.escondite.managers;

import com.Shinzrtx.escondite.Escondite;
import com.Shinzrtx.escondite.models.PlayerStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final Escondite plugin;
    private final Map<UUID, PlayerStats> statsMap = new HashMap<>();
    private File statsFile;
    private FileConfiguration statsConfig;

    public StatsManager(Escondite plugin) {
        this.plugin = plugin;
        loadStats();
    }

    public void loadStats() {
        // Asegura que la carpeta del plugin exista antes de crear el archivo
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        statsFile = new File(plugin.getDataFolder(), "stats.yml");

        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear el archivo stats.yml: " + e.getMessage());
            }
        }

        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        if (statsConfig.contains("players")) {
            for (String key : statsConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                int coins = statsConfig.getInt("players." + key + ".coins", 0);
                int kills = statsConfig.getInt("players." + key + ".kills", 0);
                int wins = statsConfig.getInt("players." + key + ".wins", 0);

                statsMap.put(uuid, new PlayerStats(coins, kills, wins));
            }
        }
    }

    public void saveStats() {
        if (statsFile == null || statsConfig == null) return;

        for (Map.Entry<UUID, PlayerStats> entry : statsMap.entrySet()) {
            String path = "players." + entry.getKey().toString();
            PlayerStats stats = entry.getValue();
            statsConfig.set(path + ".coins", stats.getCoins());
            statsConfig.set(path + ".kills", stats.getKills());
            statsConfig.set(path + ".wins", stats.getWins());
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudieron guardar las estadísticas en stats.yml: " + e.getMessage());
        }
    }

    public PlayerStats getStats(UUID uuid) {
        return statsMap.computeIfAbsent(uuid, k -> new PlayerStats(0, 0, 0));
    }
}