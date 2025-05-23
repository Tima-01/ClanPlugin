package org.plugin.clansPlugin.listeners;



import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.PlayerDataManager;

import java.util.List;

public class InventoryClickListener implements Listener {

    private final ClansPlugin plugin;

    public InventoryClickListener(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) return;
        Player player = (Player) clicker;

        if (!event.getView().getTitle().equals("Выбери клан")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        String clan = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        List<String> clans = plugin.getClanManager().getClans();
        if (!clans.contains(clan)) return;

        PlayerDataManager pdm = plugin.getPlayerDataManager();
        String current = pdm.getPlayerClan(player.getName());
        if (clan.equals(current)) {
            player.sendMessage(ChatColor.YELLOW + "Ты уже состоишь в этом клане.");
            player.closeInventory();
            return;
        }

        pdm.setPlayerClan(player.getName(), clan);
        pdm.savePlayerData();
        player.sendMessage(ChatColor.GREEN + "Ты вступил в клан: " + clan);
        player.closeInventory();
    }
}
