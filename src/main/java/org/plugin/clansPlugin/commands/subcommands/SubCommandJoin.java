package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.ClanManager;
import org.plugin.clansPlugin.managers.PlayerDataManager;

import java.util.List;

public class SubCommandJoin implements SubCommand {

    private final ClansPlugin plugin;
    private final ClanManager clanManager;
    private final PlayerDataManager pdm;

    public SubCommandJoin(ClansPlugin plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.pdm = plugin.getPlayerDataManager();
    }

    @Override
    public String[] getAliases() {
        return new String[]{"join"};
    }

    @Override
    public String getUsage() {
        return "/clan join";
    }

    @Override
    public String getDescription() {
        return "Выбрать и вступить в клан";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String playerName = player.getName();
        if (pdm.isPlayerInClan(playerName)) {
            player.sendMessage(ChatColor.RED + "Ты уже состоишь в клане. Сначала выйди: /clan leave");
            return true;
        }

        // Открываем меню выбора клана через GUI-инвентарь
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            List<String> clans = clanManager.getClans();
            int total = clans.size();
            int size = 9; // можно расширить, если будет >9 кланов
            int startIndex = (size - total) / 2;
            Inventory gui = Bukkit.createInventory(null, size, "Выбери клан");

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
                String clan = clans.get(i);
                ItemStack item = new ItemStack(bannerColors[i % bannerColors.length]);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + clan);
                item.setItemMeta(meta);
                gui.setItem(startIndex + i, item);
            }
            player.openInventory(gui);
        }, 20L);

        return true;
    }
}
