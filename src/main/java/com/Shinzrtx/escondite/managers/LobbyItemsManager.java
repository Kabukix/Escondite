package com.Shinzrtx.escondite.managers;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyItemsManager {

    public static void giveLobbyItems(Player player) {
        player.getInventory().clear();

        // Esmeralda (Lobby / Selector de Arenas)
        ItemStack emerald = XMaterial.EMERALD.parseItem();
        if (emerald != null) {
            ItemMeta meta = emerald.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Lista de Arenas " + ChatColor.GRAY + "(Clic Derecho)");
                emerald.setItemMeta(meta);
            }
            player.getInventory().setItem(0, emerald);
        }

        // Cofre (Tienda de cosméticos)
        ItemStack chest = XMaterial.CHEST.parseItem();
        if (chest != null) {
            ItemMeta meta = chest.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Tienda " + ChatColor.GRAY + "(Clic Derecho)");
                chest.setItemMeta(meta);
            }
            player.getInventory().setItem(4, chest);
        }
    }
}