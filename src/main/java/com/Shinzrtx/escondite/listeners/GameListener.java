package com.Shinzrtx.escondite.listeners;

import com.Shinzrtx.escondite.Escondite;
import com.Shinzrtx.escondite.managers.LobbyItemsManager;
import com.Shinzrtx.escondite.models.Arena;
import com.Shinzrtx.escondite.models.GameState;
import com.Shinzrtx.escondite.models.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.Collections;

public class GameListener implements Listener {

    private final Escondite plugin;

    public GameListener(Escondite plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getArenaManager().getMainLobby() != null) {
            player.teleport(plugin.getArenaManager().getMainLobby());
            LobbyItemsManager.giveLobbyItems(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("escondite.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        Arena arena = plugin.getArenaManager().getArenaOfPlayer(player);

        if (arena == null || arena.getState() == GameState.WAITING || arena.getState() == GameState.ENDING) {
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);
                if (arena != null && arena.getWaitingSpawn() != null) {
                    player.teleport(arena.getWaitingSpawn());
                } else if (plugin.getArenaManager().getMainLobby() != null) {
                    player.teleport(plugin.getArenaManager().getMainLobby());
                }
            } else {
                event.setCancelled(true);
            }
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            if (arena.getWaitingSpawn() != null) {
                player.teleport(arena.getWaitingSpawn());
            }
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;

        Arena arena = plugin.getArenaManager().getArenaOfPlayer(player);

        if (arena == null) {
            if (event.getItem().getType() == Material.EMERALD) {
                event.setCancelled(true);
                plugin.getGuiManager().openArenaSelector(player);
            } else if (event.getItem().getType() == Material.CHEST) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.GOLD + "=== [ TIENDA ] ===");
                player.sendMessage(ChatColor.YELLOW + "La tienda está en desarrollo.");
            }
        } else if (event.getItem().getType() == Material.MAGMA_CREAM) {
            event.setCancelled(true);
            plugin.getArenaManager().resetPlayerStatus(player);
            if (plugin.getArenaManager().getMainLobby() != null) {
                player.teleport(plugin.getArenaManager().getMainLobby());
            }
            LobbyItemsManager.giveLobbyItems(player);
            player.sendMessage(ChatColor.RED + "Has salido de la arena.");
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Material type = event.getItemDrop().getItemStack().getType();
        if (type == Material.EMERALD || type == Material.CHEST || type == Material.MAGMA_CREAM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().contains("Lista de Arenas")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

            String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
            String arenaName = ChatColor.stripColor(displayName).trim();

            player.closeInventory();
            plugin.getArenaManager().joinArena(player, arenaName);
            return;
        }

        Arena arena = plugin.getArenaManager().getArenaOfPlayer(player);
        if (arena == null || arena.getState() == GameState.WAITING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework) {
            event.setCancelled(true);
            return;
        }

        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        Arena arenaDamager = plugin.getArenaManager().getArenaOfPlayer(damager);
        Arena arenaVictim = plugin.getArenaManager().getArenaOfPlayer(victim);

        if (arenaDamager == null || arenaVictim == null || !arenaDamager.equals(arenaVictim)) {
            event.setCancelled(true);
            return;
        }

        if (arenaDamager.getState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        PlayerRole damagerRole = arenaDamager.getPlayers().get(damager.getUniqueId());
        PlayerRole victimRole = arenaDamager.getPlayers().get(victim.getUniqueId());

        if (damagerRole == PlayerRole.ESCONDIDO && (victimRole == PlayerRole.ESCONDIDO || victimRole == PlayerRole.BUSCADOR)) {
            event.setDamage(0);
            return;
        }

        if (damagerRole == PlayerRole.BUSCADOR && victimRole == PlayerRole.ESCONDIDO) {
            event.setCancelled(true);

            victim.setGameMode(GameMode.SPECTATOR);
            arenaDamager.getPlayers().remove(victim.getUniqueId());

            plugin.getArenaManager().getTabManager().restaurarFormatos(Collections.singletonList(victim));

            victim.sendTitle(ChatColor.RED + "moriste", "", 10, 40, 10);

            arenaDamager.broadcastToArena(ChatColor.RED + damager.getName() + ChatColor.WHITE + " asesino a " + ChatColor.YELLOW + victim.getName());

            arenaDamager.setGameTimeSeconds(arenaDamager.getGameTimeSeconds() + 30);
            arenaDamager.broadcastToArena(ChatColor.BLUE + "Se han añadido +30 segundos al reloj.");

            boolean hidersRemaining = arenaDamager.getPlayers().containsValue(PlayerRole.ESCONDIDO);
            if (!hidersRemaining) {
                plugin.getArenaManager().stopMatch(arenaDamager, "BUSCADORES");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getArenaManager().getArenaOfPlayer(player);
        if (arena != null) {
            plugin.getArenaManager().resetPlayerStatus(player);
        }
        plugin.getScoreboardManager().removeBoard(player);
    }
}