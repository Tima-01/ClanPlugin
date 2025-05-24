package org.plugin.clansPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.plugin.clansPlugin.managers.VoteManager;

public class StartVoteCommand implements CommandExecutor {

    private final VoteManager voteManager;

    public StartVoteCommand(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clan.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для этой команды.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /startvote <название_клана>");
            return true;
        }

        String clan = args[0];
        voteManager.startVote(clan);
        sender.sendMessage(ChatColor.GREEN + "Голосование за лидера в клане " + clan + " начато.");
        return true;
    }
}
