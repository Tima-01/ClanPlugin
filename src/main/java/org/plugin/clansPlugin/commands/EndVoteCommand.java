package org.plugin.clansPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.VoteManager;

public class EndVoteCommand implements CommandExecutor {

    private final VoteManager voteManager;

    public EndVoteCommand(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для этой команды.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /endvote <клан>");
            return true;
        }

        String clanName = args[0];
        voteManager.forceEndVote(clanName);

        return true;
    }
}
