package com.Shinzrtx.escondite.managers;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collection;

public class GameTabManager {

    private final boolean tabEnabled;

    public GameTabManager() {
        this.tabEnabled = Bukkit.getPluginManager().isPluginEnabled("TAB");
    }

    public void aplicarFormatosPartida(Collection<Player> escondidos, Collection<Player> asesinos) {
        if (tabEnabled) {
            aplicarConTabPlugin(escondidos, asesinos);
        } else {
            aplicarConVanillaTeams(escondidos, asesinos);
        }
    }

    public void restaurarFormatos(Collection<Player> todos) {
        if (tabEnabled) {
            restaurarConTabPlugin(todos);
        } else {
            restaurarConVanillaTeams(todos);
        }
    }

    private void aplicarConTabPlugin(Collection<Player> escondidos, Collection<Player> asesinos) {
        TabAPI tabAPI = TabAPI.getInstance();
        NameTagManager nameTagMgr = tabAPI.getNameTagManager();
        TabListFormatManager tabListMgr = tabAPI.getTabListFormatManager();

        for (Player p : asesinos) {
            TabPlayer tp = tabAPI.getPlayer(p.getUniqueId());
            if (tp == null) continue;

            if (nameTagMgr != null) {
                nameTagMgr.setPrefix(tp, "&cAsesino &f");
            }
            if (tabListMgr != null) {
                tabListMgr.setPrefix(tp, "&cAsesino &f");
            }
        }

        for (Player p : escondidos) {
            TabPlayer tp = tabAPI.getPlayer(p.getUniqueId());
            if (tp == null) continue;

            if (nameTagMgr != null) {
                nameTagMgr.hideNameTag(tp);
            }
            if (tabListMgr != null) {
                tabListMgr.setName(tp, "&k" + p.getName());
            }
        }
    }

    private void restaurarConTabPlugin(Collection<Player> todos) {
        TabAPI tabAPI = TabAPI.getInstance();
        NameTagManager nameTagMgr = tabAPI.getNameTagManager();
        TabListFormatManager tabListMgr = tabAPI.getTabListFormatManager();

        for (Player p : todos) {
            TabPlayer tp = tabAPI.getPlayer(p.getUniqueId());
            if (tp == null) continue;

            if (nameTagMgr != null) {
                nameTagMgr.setPrefix(tp, null);
                nameTagMgr.showNameTag(tp);
            }
            if (tabListMgr != null) {
                tabListMgr.setPrefix(tp, null);
                tabListMgr.setName(tp, null);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void aplicarConVanillaTeams(Collection<Player> escondidos, Collection<Player> asesinos) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        Team teamAsesino = getOrCreateTeam(board, "ESC_Asesino", ChatColor.RED + "Asesino " + ChatColor.WHITE, true);
        Team teamEscondido = getOrCreateTeam(board, "ESC_Escondido", ChatColor.MAGIC.toString(), false);

        for (Player p : asesinos) {
            teamAsesino.addEntry(p.getName());
        }
        for (Player p : escondidos) {
            teamEscondido.addEntry(p.getName());
            p.setPlayerListName(ChatColor.MAGIC + p.getName());
        }
    }

    private void restaurarConVanillaTeams(Collection<Player> todos) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team teamAsesino = board.getTeam("ESC_Asesino");
        Team teamEscondido = board.getTeam("ESC_Escondido");

        if (teamAsesino != null) teamAsesino.unregister();
        if (teamEscondido != null) teamEscondido.unregister();

        for (Player p : todos) {
            p.setPlayerListName(p.getName());
        }
    }

    @SuppressWarnings("deprecation")
    private Team getOrCreateTeam(Scoreboard board, String name, String prefix, boolean visibleName) {
        Team team = board.getTeam(name);
        if (team == null) team = board.registerNewTeam(name);
        team.setPrefix(prefix);
        if (!visibleName) {
            team.setNameTagVisibility(NameTagVisibility.NEVER);
        }
        return team;
    }
}