package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class SubCommandRemoveLeader implements SubCommand {

    private final PlayerDataManager playerDataManager;

    public SubCommandRemoveLeader(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"removeleader"};
    }

    @Override
    public String getUsage() {
        return "/clan removeleader <название_клана>";
    }

    @Override
    public String getDescription() {
        return "Удалить лидера указанного клана (только для админа)";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!player.hasPermission("clan.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return true;
        }

        String clanName = args[0];
        String leader = playerDataManager.getClanLeader(clanName);

        if (leader == null) {
            player.sendMessage(ChatColor.YELLOW + "У клана " + clanName + " нет назначенного лидера.");
            return true;
        }

        playerDataManager.setClanLeader(clanName, null);
        player.sendMessage(ChatColor.GREEN + "Лидер клана " + clanName + " был удалён.");
        return true;
    }
}
