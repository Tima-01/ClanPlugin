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
            sender.sendMessage(ChatColor.YELLOW + "/clanadmin deletebase <клан> - удалить базу клана");
            sender.sendMessage(ChatColor.YELLOW + "/clan reload" + ChatColor.WHITE + " - Перезагрузка конфигов");
            sender.sendMessage(ChatColor.YELLOW + "/endtvote <клан>" + ChatColor.WHITE + " - Досрочное успешное завершение голосования");
            sender.sendMessage(ChatColor.YELLOW + "/startvote <клан>" + ChatColor.WHITE + " - Начать голосование за нового лидера");
            sender.sendMessage(ChatColor.YELLOW + "/clan removeleader <клан>" + ChatColor.WHITE + " - Удалить лидера у клана");
            sender.sendMessage(ChatColor.YELLOW + "/clan setleader <игрок>" + ChatColor.WHITE + " - Назначить лидера клана");
            sender.sendMessage(ChatColor.YELLOW + "/addplayer <игрок> <клан>" + ChatColor.WHITE + " - Добавить участника в клан");
            sender.sendMessage(ChatColor.YELLOW + "/removeplayer <игрок>" + ChatColor.WHITE + " - Удалить участника из клана");
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
                        String leader = playerDataManager.getClanLeader(clan);
                        sender.sendMessage(ChatColor.YELLOW + "- " + clan + (leader != null ? " (Лидер: " + leader + ")" : ""));
                    }
                }
            }

            case "members" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Укажи название клана: /clanadmin members <клан>");
                    return true;
                }
                String clan = args[1];
                if (!clanManager.clanExists(clan)) {
                    sender.sendMessage(ChatColor.RED + "Клан не найден.");
                    return true;
                }
                List<String> members = playerDataManager.getClanMembers(clan);
                if (members.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "У этого клана нет участников.");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Участники клана " + clan + ":");
                    for (String member : members) {
                        String role = member.equals(playerDataManager.getClanLeader(clan)) ? " [Лидер]" : "";
                        sender.sendMessage(ChatColor.YELLOW + "- " + member + role);
                    }
                }
            }

            case "bases" -> {
                boolean hasBases = false;
                sender.sendMessage(ChatColor.GREEN + "Базы кланов:");
                for (String clan : clanManager.getClans()) {
                    int[] territory = territoryManager.getClanTerritory(clan);
                    if (territory != null) {
                        hasBases = true;
                        Location center = territoryManager.getClanBaseCenter(clan);
                        sender.sendMessage(ChatColor.YELLOW + clan + ": " +
                                "Мир: " + center.getWorld().getName() +
                                ", Центр: [" + center.getBlockX() + ", " + center.getBlockY() + ", " + center.getBlockZ() + "]" +
                                ", Границы: X[" + territory[0] + "-" + territory[2] + "], Z[" + territory[1] + "-" + territory[3] + "]");
                    }
                }
                if (!hasBases) {
                    sender.sendMessage(ChatColor.GRAY + "Нет баз.");
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
                if (!clanManager.clanExists(clan)) {
                    sender.sendMessage(ChatColor.RED + "Клан не найден.");
                    return true;
                }
                Location baseLocation = territoryManager.getClanBaseCenter(clan);
                if (baseLocation == null) {
                    sender.sendMessage(ChatColor.RED + "У этого клана нет базы.");
                } else {
                    player.teleport(baseLocation);
                    sender.sendMessage(ChatColor.GREEN + "Телепортация к базе клана " + clan);
                }
            }

            case "deletebase" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Укажи клан: /clanadmin deletebase <клан>");
                    return true;
                }
                String clan = args[1];
                if (!clanManager.clanExists(clan)) {
                    sender.sendMessage(ChatColor.RED + "Клан не найден.");
                    return true;
                }
                if (territoryManager.getClanTerritory(clan) == null) {
                    sender.sendMessage(ChatColor.RED + "У этого клана нет базы.");
                    return true;
                }
                territoryManager.deleteClanTerritory(clan);
                sender.sendMessage(ChatColor.GREEN + "База клана " + clan + " успешно удалена!");
            }

            default -> {
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда. Используй /clanadmin");
            }
        }
        return true;
    }
}