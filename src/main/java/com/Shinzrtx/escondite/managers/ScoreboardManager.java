package com.Shinzrtx.escondite.managers;

import com.Shinzrtx.escondite.Escondite;
import com.Shinzrtx.escondite.models.Arena;
import com.Shinzrtx.escondite.models.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final Escondite plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardManager(Escondite plugin) {
        this.plugin = plugin;
        startUpdater();
    }

    public void createBoard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("escondite", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "ESCONDITE");
        player.setScoreboard(board);
        boards.put(player.getUniqueId(), board);
    }

    public void removeBoard(Player player) {
        boards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private void startUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Arena arena = plugin.getArenaManager().getArenaOfPlayer(player);
                Location mainLobby = plugin.getArenaManager().getMainLobby();

                // Si está en ENDING, quitar scoreboard por completo
                if (arena != null && arena.getState() == GameState.ENDING) {
                    if (boards.containsKey(player.getUniqueId())) {
                        removeBoard(player);
                    }
                    continue;
                }

                // Si no está en arena y está fuera del mundo del lobby, quitar scoreboard
                if (arena == null) {
                    if (mainLobby == null || !player.getWorld().equals(mainLobby.getWorld())) {
                        if (boards.containsKey(player.getUniqueId())) {
                            removeBoard(player);
                        }
                        continue;
                    }
                }

                if (!boards.containsKey(player.getUniqueId())) {
                    createBoard(player);
                }

                Scoreboard board = boards.get(player.getUniqueId());
                Objective obj = board.getObjective("escondite");
                if (obj == null) continue;

                for (String entry : board.getEntries()) {
                    board.resetScores(entry);
                }

                if (arena == null) {
                    // Scoreboard del Lobby Principal
                    obj.getScore(ChatColor.GRAY + "------------------").setScore(4);
                    obj.getScore(ChatColor.WHITE + "Bienvenido: " + ChatColor.YELLOW + player.getName()).setScore(3);
                    obj.getScore(ChatColor.WHITE + "Jugadores: " + ChatColor.GREEN + Bukkit.getOnlinePlayers().size()).setScore(2);
                    obj.getScore(ChatColor.GRAY + "-------------------").setScore(1);

                } else if (arena.getState() == GameState.WAITING) {
                    // Scoreboard en Sala de Espera
                    obj.getScore(ChatColor.GRAY + "------------------").setScore(5);
                    obj.getScore(ChatColor.WHITE + "Mapa: " + ChatColor.GREEN + arena.getName()).setScore(4);
                    obj.getScore(ChatColor.WHITE + "Jugadores: " + ChatColor.YELLOW + arena.getPlayers().size() + "/" + arena.getMaxPlayers()).setScore(3);

                    if (arena.getPlayers().size() >= arena.getMinPlayers()) {
                        obj.getScore(ChatColor.WHITE + "Iniciando en: " + ChatColor.GREEN + arena.getCountdownSeconds() + "s").setScore(2);
                    } else {
                        obj.getScore(ChatColor.WHITE + "Estado: " + ChatColor.RED + "Esperando...").setScore(2);
                    }

                    obj.getScore(ChatColor.GRAY + "-------------------").setScore(1);

                } else if (arena.getState() == GameState.PLAYING) {
                    // Scoreboard en Partida INGAME
                    obj.getScore(ChatColor.GRAY + "------------------").setScore(5);
                    obj.getScore(ChatColor.WHITE + "Mapa: " + ChatColor.GREEN + arena.getName()).setScore(4);
                    obj.getScore(ChatColor.WHITE + "Tiempo: " + ChatColor.YELLOW + arena.getGameTimeSeconds() + "s").setScore(3);
                    obj.getScore(ChatColor.WHITE + "Rol: " + ChatColor.GOLD + arena.getPlayers().get(player.getUniqueId())).setScore(2);
                    obj.getScore(ChatColor.GRAY + "-------------------").setScore(1);
                }
            }
        }, 0L, 20L);
    }

    public void loadConfig() {}
}