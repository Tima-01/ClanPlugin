package org.plugin.clansPlugin.commands;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryAdjuster;

public class AddPlayerCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;

    public AddPlayerCommand(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(ChatColor.RED + "У тебя нет прав на эту команду.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.YELLOW + "Использование: /clan addplayer <игрок> <клан>");
            return true;
        }

        String playerName = args[0];
        String clanName = args[1];

        playerDataManager.setPlayerClan(playerName, clanName);
        playerDataManager.savePlayerData();

        // Расширение территории после вступления
        TerritoryAdjuster adjuster = new TerritoryAdjuster(
                playerDataManager,
                org.plugin.clansPlugin.ClansPlugin.getInstance().getTerritoryManager()
        );
        adjuster.adjustTerritory(clanName);

        sender.sendMessage(ChatColor.GREEN + "Игрок " + playerName + " добавлен в клан " + clanName + ".");

        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.isOnline()) {
            target.sendMessage(ChatColor.GREEN + "Ты был добавлен в клан " + clanName + " администратором.");
        }

        // Сообщение другим участникам клана
        String joinMessage = ChatColor.AQUA + "Игрок " + playerName + " вступил в клан.";
        for (String memberName : playerDataManager.getClanMembers(clanName)) {
            if (memberName.equalsIgnoreCase(playerName)) continue;
            Player member = Bukkit.getPlayerExact(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage(joinMessage);
            }
        }

        // Сообщение лидеру клана
        String leaderName = playerDataManager.getClanLeader(clanName);
        if (leaderName != null) {
            Player leader = Bukkit.getPlayerExact(leaderName);
            if (leader != null && leader.isOnline()) {
                leader.sendMessage(ChatColor.GREEN + "Игрок " + playerName + " вступил в клан. Территория расширена.");
            }
        }

        return true;
    }

}

