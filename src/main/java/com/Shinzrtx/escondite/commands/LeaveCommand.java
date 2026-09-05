package com.Shinzrtx.escondite.commands;

import com.Shinzrtx.escondite.Escondite;
import com.Shinzrtx.escondite.managers.LobbyItemsManager;
import com.Shinzrtx.escondite.models.Arena;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {

    private final Escondite plugin;

    public LeaveCommand(Escondite plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo lo pueden ejecutar jugadores.");
            return true;
        }

        Player player = (Player) sender;
        Arena arena = plugin.getArenaManager().getArenaOfPlayer(player);

        if (arena == null) {
            player.sendMessage(ChatColor.RED + "No estás en ninguna arena.");
            return true;
        }

        plugin.getArenaManager().resetPlayerStatus(player);

        if (plugin.getArenaManager().getMainLobby() != null) {
            player.teleport(plugin.getArenaManager().getMainLobby());
            LobbyItemsManager.giveLobbyItems(player);
        }

        player.sendMessage(ChatColor.RED + "Has salido de la arena.");
        plugin.getArenaManager().checkWaitingState(arena);

        return true;
    }
}