package org.plugin.clansPlugin.commands;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.VoteManager;

public class VoteCommand implements CommandExecutor {

    private final VoteManager voteManager;

    public VoteCommand(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 1) {
            player.sendMessage("§cИспользование: /votel <ник>");
            return true;
        }

        voteManager.vote(player.getName(), args[0]);
        return true;
    }
}
