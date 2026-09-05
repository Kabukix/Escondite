package com.Shinzrtx.escondite.managers;

import com.Shinzrtx.escondite.Escondite;
import com.Shinzrtx.escondite.models.Arena;
import com.Shinzrtx.escondite.models.GameState;
import com.Shinzrtx.escondite.models.PlayerRole;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private final Escondite plugin;
    private final GameTabManager tabManager;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<UUID, String> playerArenaMap = new HashMap<>();
    private final Map<String, Integer> waitingTimers = new HashMap<>();
    private final Map<String, Integer> gameTimers = new HashMap<>();

    private Location mainLobby;
    private File arenasFile;
    private FileConfiguration arenasConfig;
    private Team hiderTeam;

    private final Random random = new Random();
    private final Color[] fireworkColors = new Color[]{
            Color.RED, Color.BLUE, Color.LIME, Color.YELLOW,
            Color.ORANGE, Color.PURPLE, Color.AQUA, Color.FUCHSIA, Color.TEAL, Color.WHITE
    };
    private final FireworkEffect.Type[] fireworkTypes = FireworkEffect.Type.values();

    public ArenaManager(Escondite plugin) {
        this.plugin = plugin;
        this.tabManager = new GameTabManager();
        setupHiderTeam();
    }

    private void setupHiderTeam() {
        org.bukkit.scoreboard.Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        hiderTeam = board.getTeam("hiders_no_tag");
        if (hiderTeam == null) {
            hiderTeam = board.registerNewTeam("hiders_no_tag");
        }
        hiderTeam.setNameTagVisibility(NameTagVisibility.NEVER);
    }

    public Map<String, Arena> getArenas() { return arenas; }
    public GameTabManager getTabManager() { return tabManager; }

    public void createArena(String name) {
        if (!arenas.containsKey(name.toLowerCase())) {
            Arena arena = new Arena(name);
            arenas.put(name.toLowerCase(), arena);
        }
    }

    public void deleteArena(String name) {
        arenas.remove(name.toLowerCase());
        saveArenas();
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Arena getArenaOfPlayer(Player player) {
        String arenaName = playerArenaMap.get(player.getUniqueId());
        if (arenaName == null) return null;
        return arenas.get(arenaName.toLowerCase());
    }

    public void removePlayerFromArena(Player player) {
        String arenaName = playerArenaMap.remove(player.getUniqueId());
        if (arenaName != null) {
            Arena arena = arenas.get(arenaName.toLowerCase());
            if (arena != null) {
                arena.getPlayers().remove(player.getUniqueId());
            }
        }
    }

    public void joinArena(Player player, String arenaName) {
        Arena arena = getArena(arenaName);

        if (arena == null || !arena.isSaved()) {
            player.sendMessage(ChatColor.RED + "La arena no existe o no ha sido guardada con /escondite save.");
            return;
        }

        if (getArenaOfPlayer(player) != null) {
            player.sendMessage(ChatColor.RED + "Ya estás dentro de una arena.");
            return;
        }

        if (arena.getState() != GameState.WAITING) {
            player.sendMessage(ChatColor.RED + "La partida ya está en curso.");
            return;
        }

        if (arena.getPlayers().size() >= arena.getMaxPlayers()) {
            player.sendMessage(ChatColor.RED + "La arena está llena.");
            return;
        }

        arena.getPlayers().put(player.getUniqueId(), PlayerRole.ESCONDIDO);
        playerArenaMap.put(player.getUniqueId(), arena.getName());

        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(arena.getWaitingSpawn());
        player.getInventory().clear();

        ItemStack leaveItem = XMaterial.MAGMA_CREAM.parseItem();
        if (leaveItem != null) {
            ItemMeta meta = leaveItem.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Salir del Mapa " + ChatColor.GRAY + "(Clic Derecho)");
            leaveItem.setItemMeta(meta);
            player.getInventory().setItem(8, leaveItem);
        }

        broadcastToArena(arena, ChatColor.GREEN + player.getName() + " se ha unido (" + arena.getPlayers().size() + "/" + arena.getMaxPlayers() + ")");

        checkWaitingState(arena);
    }

    public void checkWaitingState(Arena arena) {
        if (arena.getState() != GameState.WAITING) return;

        if (arena.getPlayers().size() >= arena.getMinPlayers()) {
            if (!waitingTimers.containsKey(arena.getName().toLowerCase())) {
                startWaitingCountdown(arena);
            }
        } else {
            cancelWaitingTimer(arena);
        }
    }

    private void cancelWaitingTimer(Arena arena) {
        String arenaKey = arena.getName().toLowerCase();
        if (waitingTimers.containsKey(arenaKey)) {
            Bukkit.getScheduler().cancelTask(waitingTimers.remove(arenaKey));
            arena.setCountdownSeconds(30);
            broadcastToArena(arena, ChatColor.RED + "Jugadores insuficientes. Contador cancelado y reiniciado.");
        }
    }

    private void startWaitingCountdown(Arena arena) {
        arena.setCountdownSeconds(30);
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {

            if (arena.getPlayers().size() < arena.getMinPlayers()) {
                cancelWaitingTimer(arena);
                return;
            }

            int time = arena.getCountdownSeconds();

            if (time <= 5 && time > 0) {
                Sound anvilSound = XSound.BLOCK_ANVIL_USE.parseSound();
                for (UUID uuid : arena.getPlayers().keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendTitle(ChatColor.YELLOW + String.valueOf(time), "", 0, 20, 10);
                        if (anvilSound != null) p.playSound(p.getLocation(), anvilSound, 1f, 1f);
                    }
                }
            }

            if (time <= 0) {
                Bukkit.getScheduler().cancelTask(waitingTimers.remove(arena.getName().toLowerCase()));
                startMatch(arena);
                return;
            }

            arena.setCountdownSeconds(time - 1);
        }, 0L, 20L);

        waitingTimers.put(arena.getName().toLowerCase(), taskId);
    }

    public void startMatch(Arena arena) {
        arena.setState(GameState.PLAYING);

        List<UUID> playerList = new ArrayList<>(arena.getPlayers().keySet());
        Collections.shuffle(playerList);

        int seekersNeeded = Math.min(arena.getSeekersCount(), playerList.size() - 1);
        if (seekersNeeded <= 0) seekersNeeded = 1;

        List<Player> hiders = new ArrayList<>();
        List<Player> seekers = new ArrayList<>();

        for (int i = 0; i < playerList.size(); i++) {
            UUID uuid = playerList.get(i);
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;

            p.getInventory().clear();

            if (i < seekersNeeded) {
                arena.getPlayers().put(uuid, PlayerRole.BUSCADOR);
                seekers.add(p);
                p.teleport(arena.getSeekerSpawn());
                p.setWalkSpeed(0f);

                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 20, 1));
                p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "¡ERES EL BUSCADOR! Estás inmovilizado por 20 segundos.");

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (arena.getState() == GameState.PLAYING && p.isOnline()) {
                        p.setWalkSpeed(0.2f);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 4));
                        p.setGlowing(true);

                        ItemStack axe = XMaterial.DIAMOND_AXE.parseItem();
                        if (axe != null) {
                            ItemMeta meta = axe.getItemMeta();
                            meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "🪓 HACHA ASESINA 🪓");
                            List<String> lore = new ArrayList<>();
                            lore.add(ChatColor.DARK_GRAY + "-------------------------------");
                            lore.add(ChatColor.RED + "Poderosa arma forjada para la cacería.");
                            lore.add(ChatColor.GRAY + "Elimina a cualquier escondido de un golpe.");
                            lore.add(ChatColor.YELLOW + "¡Que la cacería comience!");
                            lore.add(ChatColor.DARK_GRAY + "-------------------------------");
                            meta.setLore(lore);
                            meta.addEnchant(Enchantment.DAMAGE_ALL, 10, true);
                            meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
                            meta.setUnbreakable(true);
                            axe.setItemMeta(meta);
                            p.getInventory().addItem(axe);
                        }

                        p.sendTitle(ChatColor.DARK_RED + "Encuéntralos", "", 10, 40, 10);
                        Sound dragonSound = XSound.ENTITY_ENDER_DRAGON_GROWL.parseSound();
                        if (dragonSound != null) p.playSound(p.getLocation(), dragonSound, 1f, 1f);

                        for (UUID hUuid : arena.getPlayers().keySet()) {
                            Player hPlayer = Bukkit.getPlayer(hUuid);
                            if (hPlayer != null && arena.getPlayers().get(hUuid) == PlayerRole.ESCONDIDO) {
                                hPlayer.sendTitle(ChatColor.RED + "¡El Buscador ha despertado!", "", 10, 40, 10);
                                if (dragonSound != null) hPlayer.playSound(hPlayer.getLocation(), dragonSound, 1f, 1f);
                            }
                        }

                        startInGameTimer(arena);
                    }
                }, 20 * 20L);

            } else {
                arena.getPlayers().put(uuid, PlayerRole.ESCONDIDO);
                hiders.add(p);
                p.teleport(arena.getHiderSpawn());
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "¡ERES UN ESCONDIDO! Tienes 20 segundos para esconderte.");

                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 255, false, false));
                if (hiderTeam != null) hiderTeam.addEntry(p.getName());
            }
        }

        tabManager.aplicarFormatosPartida(hiders, seekers);
    }

    private void startInGameTimer(Arena arena) {
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            arena.setGameTimeSeconds(arena.getGameTimeSeconds() - 1);

            if (arena.getGameTimeSeconds() <= 0) {
                stopMatch(arena, "ESCONDIDOS");
            }
        }, 0L, 20L);

        gameTimers.put(arena.getName().toLowerCase(), taskId);
    }

    public void stopMatch(Arena arena, String winnerRole) {
        arena.setState(GameState.ENDING);

        if (gameTimers.containsKey(arena.getName().toLowerCase())) {
            Bukkit.getScheduler().cancelTask(gameTimers.remove(arena.getName().toLowerCase()));
        }

        List<String> survivors = new ArrayList<>();
        List<String> seekers = new ArrayList<>();
        List<Player> allPlayersInArena = new ArrayList<>();

        for (Map.Entry<UUID, PlayerRole> entry : arena.getPlayers().entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null) continue;

            allPlayersInArena.add(p);
            if (entry.getValue() == PlayerRole.ESCONDIDO) {
                survivors.add(p.getName());
            } else {
                seekers.add(p.getName());
            }
        }

        String survMsg = survivors.isEmpty() ? "Ninguno" : String.join(", ", survivors);
        String seekMsg = seekers.isEmpty() ? "Ninguno" : String.join(", ", seekers);

        broadcastToArena(arena, ChatColor.GOLD + "=======================================");
        broadcastToArena(arena, ChatColor.YELLOW + "¡FIN DE LA PARTIDA!");
        broadcastToArena(arena, ChatColor.GREEN + "Supervivientes: " + ChatColor.WHITE + survMsg);
        broadcastToArena(arena, ChatColor.RED + "Buscadores: " + ChatColor.WHITE + seekMsg);
        broadcastToArena(arena, ChatColor.GOLD + "=======================================");

        int fwTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (UUID uuid : arena.getPlayers().keySet()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    spawnVictoryFirework(p.getLocation());
                }
            }
        }, 0L, 15L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getScheduler().cancelTask(fwTaskId);

            tabManager.restaurarFormatos(allPlayersInArena);

            List<UUID> allPlayers = new ArrayList<>(playerArenaMap.keySet());
            for (UUID uuid : allPlayers) {
                String aName = playerArenaMap.get(uuid);
                if (aName != null && aName.equalsIgnoreCase(arena.getName())) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        resetPlayerStatus(p);
                        if (mainLobby != null) p.teleport(mainLobby);
                        LobbyItemsManager.giveLobbyItems(p);
                    }
                }
            }

            arena.getPlayers().clear();
            arena.setState(GameState.WAITING);
            arena.setCountdownSeconds(30);
        }, 200L);
    }

    public void resetPlayerStatus(Player player) {
        Arena arena = getArenaOfPlayer(player);

        tabManager.restaurarFormatos(Collections.singletonList(player));

        player.setGlowing(false);
        player.setWalkSpeed(0.2f);
        player.setGameMode(GameMode.SURVIVAL);
        if (hiderTeam != null) hiderTeam.removeEntry(player.getName());
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.getInventory().clear();

        removePlayerFromArena(player);

        if (arena != null) {
            broadcastToArena(arena, ChatColor.RED + player.getName() + " se ha salido (" + arena.getPlayers().size() + "/" + arena.getMaxPlayers() + ")");
            checkWaitingState(arena);
        }
    }

    private void spawnVictoryFirework(Location loc) {
        double offsetX = (random.nextDouble() - 0.5) * 4;
        double offsetZ = (random.nextDouble() - 0.5) * 4;
        Location spawnLoc = loc.clone().add(offsetX, 1, offsetZ);

        Firework fw = spawnLoc.getWorld().spawn(spawnLoc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();

        Color color1 = fireworkColors[random.nextInt(fireworkColors.length)];
        Color color2 = fireworkColors[random.nextInt(fireworkColors.length)];
        FireworkEffect.Type type = fireworkTypes[random.nextInt(fireworkTypes.length)];

        meta.addEffect(FireworkEffect.builder()
                .withColor(color1)
                .withFade(color2)
                .with(type)
                .flicker(random.nextBoolean())
                .trail(random.nextBoolean())
                .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);

        Bukkit.getScheduler().runTaskLater(plugin, fw::detonate, 2L);
    }

    public void broadcastToArena(Arena arena, String msg) {
        arena.broadcastToArena(msg);
    }

    public Location getMainLobby() { return mainLobby; }
    public void setMainLobby(Location mainLobby) { this.mainLobby = mainLobby; saveArenas(); }

    public void loadArenas() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            try { arenasFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);

        if (arenasConfig.contains("main-lobby")) mainLobby = arenasConfig.getLocation("main-lobby");

        if (arenasConfig.contains("arenas")) {
            for (String key : arenasConfig.getConfigurationSection("arenas").getKeys(false)) {
                Arena arena = new Arena(key);
                arena.setMinPlayers(arenasConfig.getInt("arenas." + key + ".min", 2));
                arena.setMaxPlayers(arenasConfig.getInt("arenas." + key + ".max", 12));
                arena.setSeekersCount(arenasConfig.getInt("arenas." + key + ".seekers", 1));
                arena.setGameTimeSeconds(arenasConfig.getInt("arenas." + key + ".time", 300));
                arena.setSaved(arenasConfig.getBoolean("arenas." + key + ".saved", false));
                arena.setWaitingSpawn(arenasConfig.getLocation("arenas." + key + ".spawns.waiting"));
                arena.setSeekerSpawn(arenasConfig.getLocation("arenas." + key + ".spawns.seeker"));
                arena.setHiderSpawn(arenasConfig.getLocation("arenas." + key + ".spawns.hider"));
                arenas.put(key.toLowerCase(), arena);
            }
        }
    }

    public void saveArenas() {
        if (arenasFile == null || arenasConfig == null) return;
        arenasConfig.set("main-lobby", mainLobby);
        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName();
            arenasConfig.set(path + ".min", arena.getMinPlayers());
            arenasConfig.set(path + ".max", arena.getMaxPlayers());
            arenasConfig.set(path + ".seekers", arena.getSeekersCount());
            arenasConfig.set(path + ".time", arena.getGameTimeSeconds());
            arenasConfig.set(path + ".saved", arena.isSaved());
            arenasConfig.set(path + ".spawns.waiting", arena.getWaitingSpawn());
            arenasConfig.set(path + ".spawns.seeker", arena.getSeekerSpawn());
            arenasConfig.set(path + ".spawns.hider", arena.getHiderSpawn());
        }
        try { arenasConfig.save(arenasFile); } catch (IOException e) { e.printStackTrace(); }
    }
}