package org.plugin.clansPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class RemoveLeaderCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;

    public RemoveLeaderCommand(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clan.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /clan removeleader <название_клана>");
            return true;
        }

        String clanName = args[0];
        String leader = playerDataManager.getClanLeader(clanName);

        if (leader == null) {
            sender.sendMessage(ChatColor.YELLOW + "У клана " + clanName + " нет назначенного лидера.");
            return true;
        }

        playerDataManager.setClanLeader(clanName, null);
        sender.sendMessage(ChatColor.GREEN + "Лидер клана " + clanName + " был удалён.");
        return true;
    }
}

