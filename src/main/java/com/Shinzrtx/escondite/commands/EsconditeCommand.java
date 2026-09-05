package com.Shinzrtx.escondite.commands;

import com.Shinzrtx.escondite.Escondite;
import com.Shinzrtx.escondite.models.Arena;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EsconditeCommand implements CommandExecutor {

    private final Escondite plugin;

    public EsconditeCommand(Escondite plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                plugin.getScoreboardManager().loadConfig();
                sender.sendMessage(ChatColor.GREEN + "¡Configuración recargada!");
            }
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.GOLD + "=== Comandos de Escondite ===");
            player.sendMessage(ChatColor.YELLOW + "/escondite list " + ChatColor.GRAY + "- Abrir lista de arenas");
            player.sendMessage(ChatColor.YELLOW + "/escondite join <mapa> " + ChatColor.GRAY + "- Unirse a un mapa");
            player.sendMessage(ChatColor.YELLOW + "/leave " + ChatColor.GRAY + "- Salir del mapa actual");

            if (player.hasPermission("escondite.admin")) {
                player.sendMessage(ChatColor.RED + "=== Comandos de Administrador ===");
                player.sendMessage(ChatColor.RED + "/escondite setlobby " + ChatColor.GRAY + "- Guardar lobby principal");
                player.sendMessage(ChatColor.RED + "/escondite create <mapa> " + ChatColor.GRAY + "- Crear borrador de arena");
                player.sendMessage(ChatColor.RED + "/escondite delete <mapa> " + ChatColor.GRAY + "- Eliminar arena");
                player.sendMessage(ChatColor.RED + "/escondite setmin <mapa> <min> " + ChatColor.GRAY + "- Mínimo de jugadores");
                player.sendMessage(ChatColor.RED + "/escondite setmax <mapa> <max> " + ChatColor.GRAY + "- Máximo de jugadores");
                player.sendMessage(ChatColor.RED + "/escondite setseekers <mapa> <cantidad> " + ChatColor.GRAY + "- Cantidad de buscadores");
                player.sendMessage(ChatColor.RED + "/escondite settime <mapa> <segundos> " + ChatColor.GRAY + "- Tiempo de juego");
                player.sendMessage(ChatColor.RED + "/escondite setwait <mapa> " + ChatColor.GRAY + "- Spawn de espera");
                player.sendMessage(ChatColor.RED + "/escondite setseekerspawn <mapa> " + ChatColor.GRAY + "- Spawn del buscador");
                player.sendMessage(ChatColor.RED + "/escondite sethiderspawn <mapa> " + ChatColor.GRAY + "- Spawn de escondidos");
                player.sendMessage(ChatColor.RED + "/escondite save <mapa> " + ChatColor.GRAY + "- Activar y guardar la arena");
                player.sendMessage(ChatColor.RED + "/escondite start <mapa> " + ChatColor.GRAY + "- Forzar inicio de partida");
                player.sendMessage(ChatColor.RED + "/escondite stop <mapa> " + ChatColor.GRAY + "- Forzar detención de partida");
                player.sendMessage(ChatColor.RED + "/escondite reload " + ChatColor.GRAY + "- Recargar archivos");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            plugin.getGuiManager().openArenaSelector(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("join") && args.length > 1) {
            plugin.getArenaManager().joinArena(player, args[1]);
            return true;
        }

        if (!player.hasPermission("escondite.admin")) {
            player.sendMessage(ChatColor.RED + "No tienes permisos para usar este comando.");
            return true;
        }

        if (args[0].equalsIgnoreCase("setlobby")) {
            plugin.getArenaManager().setMainLobby(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Lobby principal guardado.");
            return true;
        }

        if (args[0].equalsIgnoreCase("create") && args.length > 1) {
            plugin.getArenaManager().createArena(args[1]);
            player.sendMessage(ChatColor.GREEN + "Borrador de la arena '" + args[1] + "' creado.");
            player.sendMessage(ChatColor.YELLOW + "Recuerda ejecutar /escondite save " + args[1] + " para guardarla.");
            return true;
        }

        if (args[0].equalsIgnoreCase("delete") && args.length > 1) {
            plugin.getArenaManager().deleteArena(args[1]);
            player.sendMessage(ChatColor.GREEN + "Arena '" + args[1] + "' eliminada.");
            return true;
        }

        Arena arena = args.length > 1 ? plugin.getArenaManager().getArena(args[1]) : null;

        if (args[0].equalsIgnoreCase("setmin") && args.length > 2) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            arena.setMinPlayers(Integer.parseInt(args[2]));
            player.sendMessage(ChatColor.GREEN + "Mínimo de jugadores fijado en " + args[2]);
            return true;
        }

        if (args[0].equalsIgnoreCase("setmax") && args.length > 2) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            arena.setMaxPlayers(Integer.parseInt(args[2]));
            player.sendMessage(ChatColor.GREEN + "Máximo de jugadores fijado en " + args[2]);
            return true;
        }

        if (args[0].equalsIgnoreCase("setseekers") && args.length > 2) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            arena.setSeekersCount(Integer.parseInt(args[2]));
            player.sendMessage(ChatColor.GREEN + "Buscadores fijados en " + args[2]);
            return true;
        }

        if (args[0].equalsIgnoreCase("settime") && args.length > 2) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            arena.setGameTimeSeconds(Integer.parseInt(args[2]));
            player.sendMessage(ChatColor.GREEN + "Tiempo fijado en " + args[2] + " segundos.");
            return true;
        }

        if (args[0].equalsIgnoreCase("setwait") && args.length > 1) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            arena.setWaitingSpawn(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Spawn de espera establecido.");
            return true;
        }

        if (args[0].equalsIgnoreCase("setseekerspawn") && args.length > 1) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            arena.setSeekerSpawn(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Spawn de Buscadores establecido.");
            return true;
        }

        if (args[0].equalsIgnoreCase("sethiderspawn") && args.length > 1) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            arena.setHiderSpawn(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Spawn de Escondidos establecido.");
            return true;
        }

        if (args[0].equalsIgnoreCase("save") && args.length > 1) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            if (arena.getWaitingSpawn() == null || arena.getSeekerSpawn() == null || arena.getHiderSpawn() == null) {
                player.sendMessage(ChatColor.RED + "Debes establecer todos los spawns (setwait, setseekerspawn, sethiderspawn) antes de guardar.");
                return true;
            }
            arena.setSaved(true);
            plugin.getArenaManager().saveArenas();
            player.sendMessage(ChatColor.GREEN + "¡Arena '" + arena.getName() + "' guardada y ACTIVADA correctamente!");
            return true;
        }

        if (args[0].equalsIgnoreCase("start") && args.length > 1) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            plugin.getArenaManager().startMatch(arena);
            player.sendMessage(ChatColor.GREEN + "Partida iniciada forzadamente.");
            return true;
        }

        if (args[0].equalsIgnoreCase("stop") && args.length > 1) {
            if (arena == null) { player.sendMessage(ChatColor.RED + "La arena no existe."); return true; }
            plugin.getArenaManager().stopMatch(arena, "ADMIN");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getScoreboardManager().loadConfig();
            player.sendMessage(ChatColor.GREEN + "¡Configuración recargada!");
            return true;
        }

        return true;
    }
}