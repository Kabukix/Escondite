package com.Shinzrtx.escondite.managers;

import com.Shinzrtx.escondite.models.Arena;
import com.Shinzrtx.escondite.models.GameState;
import com.Shinzrtx.escondite.models.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GameTimer extends BukkitRunnable {

    private final Arena arena;
    private int countdown = 30; // 30 segundos de espera en el lobby

    public GameTimer(Arena arena) {
        this.arena = arena;
    }

    @Override
    public void run() {
        if (arena.getPlayers().size() < 2) {
            broadcast(ChatColor.RED + "Partida cancelada: Se necesitan al menos 2 jugadores.");
            arena.setState(GameState.WAITING);
            cancel();
            return;
        }

        if (countdown == 30 || countdown == 15 || (countdown <= 5 && countdown > 0)) {
            broadcast(ChatColor.YELLOW + "La partida empieza en " + ChatColor.RED + countdown + ChatColor.YELLOW + " segundos...");
        }

        if (countdown == 0) {
            startGame();
            cancel();
            return;
        }

        countdown--;
    }

    private void startGame() {
        arena.setState(GameState.PLAYING);
        broadcast(ChatColor.GREEN + "¡La partida ha comenzado!");

        List<UUID> playerList = new ArrayList<>(arena.getPlayers().keySet());
        Random random = new Random();

        // Seleccionar 1 buscador al azar
        UUID seekerUUID = playerList.get(random.nextInt(playerList.size()));

        for (UUID uuid : playerList) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            if (uuid.equals(seekerUUID)) {
                arena.getPlayers().put(uuid, PlayerRole.BUSCADOR);
                if (arena.getSeekerSpawn() != null) player.teleport(arena.getSeekerSpawn());
                player.sendMessage(ChatColor.RED + "¡Eres el BUSCADOR! Espera a que los escondidos se oculten.");
            } else {
                arena.getPlayers().put(uuid, PlayerRole.ESCONDIDO);
                if (arena.getHiderSpawn() != null) player.teleport(arena.getHiderSpawn());
                player.sendMessage(ChatColor.GREEN + "¡Eres ESCONDIDO! Tienes tiempo para buscar un buen escondite.");
            }
        }
    }

    private void broadcast(String message) {
        for (UUID uuid : arena.getPlayers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }
}