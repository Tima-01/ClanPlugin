package org.plugin.clansPlugin.listeners;



import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.buffs.ClanBuff;
import org.plugin.clansPlugin.gui.BuffSelectionGUI;
import org.plugin.clansPlugin.managers.PlayerDataManager;

import java.util.Arrays;
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

        String title = event.getView().getTitle();

        // Обработка выбора клана
        if (title.equals("Выбери клан")) {
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

            int updatedSize = pdm.getClanMembers(clan).size();
            plugin.getTerritoryManager().adjustClanTerritorySize(clan, updatedSize);

            player.closeInventory();
            return;
        }

        // Обработка выбора баффа
        if (title.equals(ChatColor.DARK_GREEN + "Выбор духа клана")) {
            event.setCancelled(true);

            if (!plugin.getPlayerDataManager().getClanLeader(plugin.getPlayerDataManager().getPlayerClan(player.getName()))
                    .equalsIgnoreCase(player.getName())) {
                player.sendMessage(ChatColor.RED + "Только лидер клана может выбрать бафф.");
                player.closeInventory();
                return;
            }

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            ClanBuff selectedBuff = Arrays.stream(ClanBuff.values())
                    .filter(buff -> buff.getDisplayName().equals(name))
                    .findFirst().orElse(null);

            if (selectedBuff == null) return;

            String clanId = plugin.getPlayerDataManager().getPlayerClan(player.getName());

            if (!plugin.getClanBuffManager().canChangeBuff(clanId)) {
                player.sendMessage(ChatColor.RED + "Слишком рано менять бафф. Подождите немного.");
                player.closeInventory();
                return;
            }

            plugin.getClanBuffManager().setClanBuff(clanId, selectedBuff);
            player.sendMessage(ChatColor.GREEN + "Выбран бафф: " + selectedBuff.getDisplayName());
            player.closeInventory();
        }
    }
}
