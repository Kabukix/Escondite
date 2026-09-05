package com.Shinzrtx.escondite.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.*;

public class Arena {
    private final String name;
    private GameState state = GameState.WAITING;
    private int minPlayers = 2;
    private int maxPlayers = 12;
    private int seekersCount = 1;
    private int gameTimeSeconds = 300;
    private int countdownSeconds = 30;
    private boolean saved = false;

    private Location waitingSpawn;
    private Location seekerSpawn;
    private Location hiderSpawn;

    private final Map<UUID, PlayerRole> players = new HashMap<>();

    public Arena(String name) {
        this.name = name;
    }

    public void broadcastToArena(String message) {
        for (UUID uuid : players.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(message);
            }
        }
    }

    public String getName() { return name; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public int getMinPlayers() { return minPlayers; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public int getSeekersCount() { return seekersCount; }
    public void setSeekersCount(int seekersCount) { this.seekersCount = seekersCount; }
    public int getGameTimeSeconds() { return gameTimeSeconds; }
    public void setGameTimeSeconds(int gameTimeSeconds) { this.gameTimeSeconds = gameTimeSeconds; }
    public int getCountdownSeconds() { return countdownSeconds; }
    public void setCountdownSeconds(int countdownSeconds) { this.countdownSeconds = countdownSeconds; }
    public boolean isSaved() { return saved; }
    public void setSaved(boolean saved) { this.saved = saved; }

    public Location getWaitingSpawn() { return waitingSpawn; }
    public void setWaitingSpawn(Location waitingSpawn) { this.waitingSpawn = waitingSpawn; }
    public Location getSeekerSpawn() { return seekerSpawn; }
    public void setSeekerSpawn(Location seekerSpawn) { this.seekerSpawn = seekerSpawn; }
    public Location getHiderSpawn() { return hiderSpawn; }
    public void setHiderSpawn(Location hiderSpawn) { this.hiderSpawn = hiderSpawn; }

    public Map<UUID, PlayerRole> getPlayers() { return players; }
}