package com.Shinzrtx.escondite;

import com.Shinzrtx.escondite.commands.EsconditeCommand;
import com.Shinzrtx.escondite.commands.LeaveCommand;
import com.Shinzrtx.escondite.listeners.GameListener;
import com.Shinzrtx.escondite.managers.ArenaManager;
import com.Shinzrtx.escondite.managers.GuiManager;
import com.Shinzrtx.escondite.managers.ScoreboardManager;
import com.Shinzrtx.escondite.managers.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Escondite extends JavaPlugin {

    private static Escondite instance;
    private ArenaManager arenaManager;
    private ScoreboardManager scoreboardManager;
    private GuiManager guiManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;
        this.statsManager = new StatsManager(this);
        this.arenaManager = new ArenaManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.guiManager = new GuiManager(this);

        saveDefaultConfig();
        arenaManager.loadArenas();

        if (getCommand("escondite") != null) getCommand("escondite").setExecutor(new EsconditeCommand(this));
        if (getCommand("leave") != null) getCommand("leave").setExecutor(new LeaveCommand(this));

        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        getLogger().info("==========================================");
        getLogger().info("   ¡Plugin Escondite habilitado con éxito!");
        getLogger().info("==========================================");
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) arenaManager.saveArenas();
        if (statsManager != null) statsManager.saveStats();
    }

    public static Escondite getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public StatsManager getStatsManager() { return statsManager; }
}