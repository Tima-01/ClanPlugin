package org.plugin.clansPlugin.commands;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;

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

        sender.sendMessage(ChatColor.GREEN + "Игрок " + playerName + " добавлен в клан " + clanName + ".");

        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.isOnline()) {
            target.sendMessage(ChatColor.GREEN + "Ты был добавлен в клан " + clanName + " администратором.");
        }

        return true;
    }
}

