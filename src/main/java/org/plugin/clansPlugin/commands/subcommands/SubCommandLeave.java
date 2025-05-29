package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryAdjuster;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.List;

public class SubCommandLeave implements SubCommand {

    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;

    public SubCommandLeave(PlayerDataManager pdm, TerritoryManager territoryManager) {
        this.pdm = pdm;
        this.territoryManager = territoryManager;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"leave"};
    }

    @Override
    public String getUsage() {
        return "/clan leave";
    }

    @Override
    public String getDescription() {
        return "Покинуть текущий клан";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String playerName = player.getName();
        if (!pdm.isPlayerInClan(playerName)) {
            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
            return true;
        }

        String clanName = pdm.getPlayerClan(playerName);
        String currentLeader = pdm.getClanLeader(clanName);

        // Удаляем игрока из клана
        pdm.removePlayerFromClan(playerName);
        if (playerName.equalsIgnoreCase(currentLeader)) {
            pdm.setClanLeader(clanName, null);
            player.sendMessage(ChatColor.RED + "Ты был лидером клана. Лидерство снято.");
        }

        // Пересчёт территории
        List<String> members = pdm.getClanMembers(clanName);
        TerritoryAdjuster adjuster = new TerritoryAdjuster(pdm, territoryManager);
        adjuster.adjustTerritory(clanName);

        player.sendMessage(ChatColor.YELLOW + "Ты покинул клан.");

        pdm.savePlayerData();
        return true;
    }
}
