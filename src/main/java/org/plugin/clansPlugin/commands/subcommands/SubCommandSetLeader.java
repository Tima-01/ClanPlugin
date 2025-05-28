package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class SubCommandSetLeader implements SubCommand {

    private final PlayerDataManager pdm;

    public SubCommandSetLeader(PlayerDataManager pdm) {
        this.pdm = pdm;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"setleader"};
    }

    @Override
    public String getUsage() {
        return "/clan setleader <игрок>";
    }

    @Override
    public String getDescription() {
        return "Назначить нового лидера клана (только админ)";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!player.hasPermission("clan.admin")) {
            player.sendMessage(ChatColor.RED + "У тебя нет прав использовать эту команду.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String targetName = args[0];
        String targetClan = pdm.getPlayerClan(targetName);
        if (targetClan == null) {
            player.sendMessage(ChatColor.RED + "Игрок не состоит в клане.");
            return true;
        }

        pdm.setClanLeader(targetClan, targetName);
        pdm.savePlayerData();
        player.sendMessage(ChatColor.GREEN + "Игрок " + targetName + " назначен лидером клана " + targetClan + ".");
        return true;
    }
}
