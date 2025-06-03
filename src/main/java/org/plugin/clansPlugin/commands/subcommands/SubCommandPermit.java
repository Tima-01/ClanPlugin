package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class SubCommandPermit implements SubCommand {

    private final PlayerDataManager pdm;

    public SubCommandPermit(PlayerDataManager pdm) {
        this.pdm = pdm;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"permit"};
    }

    @Override
    public String getUsage() {
        return "/clan permit <ник> <команда>";
    }

    @Override
    public String getDescription() {
        return "Выдать разрешение участнику клана на команду";
    }

    @Override
    public boolean execute(Player sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String targetName = args[0];
        String permissionKey = args[1].toLowerCase();

        String clan = pdm.getPlayerClan(sender.getName());
        if (clan == null || !pdm.getClanLeader(clan).equalsIgnoreCase(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "Только лидер клана может выдавать права.");
            return true;
        }

        if (!pdm.isPlayerInClan(targetName) || !pdm.getPlayerClan(targetName).equals(clan)) {
            sender.sendMessage(ChatColor.RED + "Игрок не состоит в вашем клане.");
            return true;
        }

        pdm.setPermission(targetName, permissionKey, true);
        sender.sendMessage(ChatColor.GREEN + "Права на команду '" + permissionKey + "' выданы игроку " + targetName + ".");
        return true;
    }
}
