package org.plugin.clansPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class ClanPvpCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;

    public ClanPvpCommand(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }

        String playerName = player.getName();
        String clanName = playerDataManager.getPlayerClan(playerName);

        if (clanName == null) {
            player.sendMessage(ChatColor.RED + "Вы не состоите в клане.");
            return true;
        }

        if (!playerName.equals(playerDataManager.getClanLeader(clanName))) {
            player.sendMessage(ChatColor.RED + "Только лидер клана может включать или отключать PVP.");
            return true;
        }

        if (args.length != 1 || (!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off"))) {
            player.sendMessage(ChatColor.RED + "Использование: /clanpvp <on|off>");
            return true;
        }

        boolean enable = args[0].equalsIgnoreCase("on");
        playerDataManager.setClanPvp(clanName, enable);
        player.sendMessage(ChatColor.GREEN + "PVP между соклановцами " + (enable ? "включено." : "отключено."));
        return true;
    }
}
