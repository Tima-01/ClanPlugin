package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class SubCommandDeleteBase implements SubCommand {

    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;

    public SubCommandDeleteBase(PlayerDataManager pdm, TerritoryManager territoryManager) {
        this.pdm = pdm;
        this.territoryManager = territoryManager;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"deletebase"};
    }

    @Override
    public String getUsage() {
        return "/clan deletebase";
    }

    @Override
    public String getDescription() {
        return "Удалить основную базу клана (только лидер)";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String playerName = player.getName();
        String clanName = pdm.getPlayerClan(playerName);
        if (clanName == null) {
            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
            return true;
        }

        String leader = pdm.getClanLeader(clanName);
        if (!playerName.equalsIgnoreCase(leader)) {
            player.sendMessage(ChatColor.RED + "Только лидер клана может удалять базу.");
            return true;
        }

        if (territoryManager.getClanTerritory(clanName) == null) {
            player.sendMessage(ChatColor.RED + "У вашего клана нет базы для удаления.");
            return true;
        }

        territoryManager.deleteClanTerritory(clanName);
        player.sendMessage(ChatColor.GREEN + "База клана успешно удалена!");
        return true;
    }
}
