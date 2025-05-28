package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.entity.Player;

public interface SubCommand {
    String[] getAliases();
    String getUsage();
    String getDescription();
    boolean execute(Player player, String[] args);
}
