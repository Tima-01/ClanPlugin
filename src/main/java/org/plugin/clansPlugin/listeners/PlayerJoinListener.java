package org.plugin.clansPlugin.listeners;



import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.PlayerDataManager;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final ClansPlugin plugin;

    public PlayerJoinListener(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager pdm = plugin.getPlayerDataManager();

        // Если игрока ещё нет в файле
        if (!pdm.isPlayerInClan(player.getName())) {
            // Открываем меню выбора клана через 1 секунду (20 тиков)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Чтобы не было проблем с синхронностью GUI, оборачиваем внутрь runTask:
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // Открываем инвентарь
                    List<String> clans = plugin.getClanManager().getClans();
                    int total = clans.size();
                    int startIndex = (9 - total) / 2;
                    org.bukkit.inventory.Inventory gui = Bukkit.createInventory(null, 9, "Выбери клан");
                    org.bukkit.Material[] bannerColors = {
                            org.bukkit.Material.RED_BANNER,
                            org.bukkit.Material.BLUE_BANNER,
                            org.bukkit.Material.GREEN_BANNER,
                            org.bukkit.Material.YELLOW_BANNER,
                            org.bukkit.Material.PURPLE_BANNER,
                            org.bukkit.Material.BLACK_BANNER,
                            org.bukkit.Material.WHITE_BANNER,
                            org.bukkit.Material.ORANGE_BANNER,
                            org.bukkit.Material.CYAN_BANNER
                    };

                    for (int i = 0; i < total; i++) {
                        String clanName = clans.get(i);
                        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(bannerColors[i % bannerColors.length]);
                        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(ChatColor.AQUA + clanName);
                        item.setItemMeta(meta);

                        gui.setItem(startIndex + i, item);
                    }
                    player.openInventory(gui);
                });
            }, 20L);
        } else {
            String clanName = pdm.getPlayerClan(player.getName());
            player.sendMessage(ChatColor.YELLOW + "Ты в клане: " + clanName);
        }
    }
}
