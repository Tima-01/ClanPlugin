package org.plugin.clansPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.ClanManager;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.List;
import java.util.Set;

public class ClanAdminCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;
    private final TerritoryManager territoryManager;
    private final ClanManager clanManager;

    public ClanAdminCommand(PlayerDataManager playerDataManager, TerritoryManager territoryManager, ClanManager clanManager) {
        this.playerDataManager = playerDataManager;
        this.territoryManager = territoryManager;
        this.clanManager = clanManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(ChatColor.RED + "У тебя нет прав для этой команды.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Использование:");
            sender.sendMessage(ChatColor.YELLOW + "/clanadmin list - список всех кланов");
            sender.sendMessage(ChatColor.YELLOW + "/clanadmin members <клан> - список участников клана");
            sender.sendMessage(ChatColor.YELLOW + "/clanadmin bases - список баз с координатами");
            sender.sendMessage(ChatColor.YELLOW + "/clanadmin tpbase <клан> - телепорт к базе");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "list" -> {
                List<String> allClans = clanManager.getClans();
                if (allClans == null || allClans.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "Кланы не найдены.");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Список кланов:");
                    for (String clan : allClans) {
                        sender.sendMessage(ChatColor.YELLOW + "- " + clan);
                    }
                }
            }

            case "members" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Укажи название клана: /clanadmin members <клан>");
                    return true;
                }
                String clan = args[1];
                List<String> members = playerDataManager.getClanMembers(clan);
                if (members.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "У этого клана нет участников.");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Участники клана " + clan + ":");
                    for (String member : members) {
                        sender.sendMessage(ChatColor.YELLOW + "- " + member);
                    }
                }
            }

            case "bases" -> {
                Set<String> clansWithBases = territoryManager.getAllClanBases();
                if (clansWithBases.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "Нет баз.");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "Базы кланов:");
                for (String clan : clansWithBases) {
                    Location base = territoryManager.getBaseLocation(clan);
                    if (base != null) {
                        sender.sendMessage(ChatColor.YELLOW + clan + ": " + base.getWorld().getName()
                                + " [" + base.getBlockX() + ", " + base.getBlockY() + ", " + base.getBlockZ() + "]");
                    }
                }
            }

            case "tpbase" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Только игрок может телепортироваться.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Укажи клан: /clanadmin tpbase <клан>");
                    return true;
                }
                String clan = args[1];
                Location baseLocation = territoryManager.getBaseLocation(clan);
                if (baseLocation == null) {
                    sender.sendMessage(ChatColor.RED + "База этого клана не найдена.");
                } else {
                    player.teleport(baseLocation);
                    sender.sendMessage(ChatColor.GREEN + "Телепортация к базе клана " + clan);
                }
            }

            default -> {
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда. Используй /clanadmin");
            }
        }

        return true;
    }
}
