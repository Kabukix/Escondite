package com.Shinzrtx.escondite.models;

public class PlayerStats {

    private int kills;
    private int wins;
    private int coins;

    public PlayerStats(int kills, int wins, int coins) {
        this.kills = kills;
        this.wins = wins;
        this.coins = coins;
    }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void addKill() { this.kills++; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public void addWin() { this.wins++; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    public void addCoins(int amount) { this.coins += amount; }
}