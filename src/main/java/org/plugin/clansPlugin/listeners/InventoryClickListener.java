package org.plugin.clansPlugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.buffs.ClanBuff;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryAdjuster;

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
            String currentClan = pdm.getPlayerClan(player.getName());

            // Если игрок уже в этом клане
            if (clan.equals(currentClan)) {
                player.sendMessage(ChatColor.YELLOW + "Ты уже состоишь в этом клане.");
                player.closeInventory();
                return;
            }

            // Если игрок состоит в другом клане - выходим из него
            if (currentClan != null) {
                pdm.removePlayerFromClan(player.getName());

                // Если игрок был лидером - снимаем лидерство
                if (player.getName().equalsIgnoreCase(pdm.getClanLeader(currentClan))) {
                    pdm.setClanLeader(currentClan, null);
                }

                // Если в клане не осталось участников - удаляем территорию
                if (pdm.getClanMembers(currentClan).isEmpty()) {
                    plugin.getTerritoryManager().deleteClanTerritory(currentClan);
                }
            }
            // Вступаем в новый клан
            pdm.setPlayerClan(player.getName(), clan);
            pdm.savePlayerData();

            // Пересчёт территории после вступления
            TerritoryAdjuster adjuster = new TerritoryAdjuster(plugin.getPlayerDataManager(), plugin.getTerritoryManager());
            adjuster.adjustTerritory(clan);

            // Сообщение в клановый чат
            String joinMessage = ChatColor.AQUA + "Игрок " + player.getName() + " вступил в клан.";
            for (String memberName : pdm.getClanMembers(clan)) {
                Player member = Bukkit.getPlayerExact(memberName);
                if (member != null && member.isOnline()) {
                    member.sendMessage(joinMessage);
                }
            }

            // Сообщение лидеру
            String leaderName = pdm.getClanLeader(clan);
            if (leaderName != null) {
                Player leader = Bukkit.getPlayerExact(leaderName);
                if (leader != null && leader.isOnline()) {
                    leader.sendMessage(ChatColor.GREEN + "Игрок " + player.getName() + " вступил в клан. Территория расширена.");
                }
            }

// Сообщение самому игроку
            player.sendMessage(ChatColor.GREEN + "Ты вступил в клан: " + clan);
            player.closeInventory();
            return;
        }

        // Обработка выбора баффа
        if (title.equals(ChatColor.DARK_GREEN + "Выбор духа клана")) {
            event.setCancelled(true);

            String playerClan = plugin.getPlayerDataManager().getPlayerClan(player.getName());
            if (playerClan == null) {
                player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                player.closeInventory();
                return;
            }

            if (!player.getName().equalsIgnoreCase(plugin.getPlayerDataManager().getClanLeader(playerClan))) {
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

            if (!plugin.getClanBuffManager().canChangeBuff(playerClan)) {
                player.sendMessage(ChatColor.RED + "Слишком рано менять бафф. Подождите немного.");
                player.closeInventory();
                return;
            }

            plugin.getClanBuffManager().setClanBuff(playerClan, selectedBuff);
            player.sendMessage(ChatColor.GREEN + "Бафф изменён на: " + selectedBuff.getDisplayName());
            player.closeInventory();
        }
    }
}