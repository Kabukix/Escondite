package com.Shinzrtx.escondite.managers;

import com.Shinzrtx.escondite.Escondite;
import com.Shinzrtx.escondite.models.Arena;
import com.Shinzrtx.escondite.models.GameState;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {

    private final Escondite plugin;

    public GuiManager(Escondite plugin) {
        this.plugin = plugin;
    }

    public void openArenaSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Lista de Arenas");

        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (!arena.isSaved()) continue;

            ItemStack item;
            if (arena.getState() == GameState.WAITING) {
                item = XMaterial.LIME_TERRACOTTA.parseItem();
            } else {
                item = XMaterial.RED_TERRACOTTA.parseItem();
            }

            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + arena.getName());

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Estado: " + (arena.getState() == GameState.WAITING ? ChatColor.GREEN + "Esperando" : ChatColor.RED + "En juego"));
                lore.add(ChatColor.GRAY + "Jugadores: " + ChatColor.YELLOW + arena.getPlayers().size() + "/" + arena.getMaxPlayers());
                lore.add("");
                lore.add(arena.getState() == GameState.WAITING ? ChatColor.YELLOW + "¡Haz clic para unirte!" : ChatColor.RED + "Partida en curso");

                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.addItem(item);
            }
        }

        player.openInventory(inv);
    }
}