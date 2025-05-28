package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.Arrays;
import java.util.List;

public class SubCommandTerritories implements SubCommand {

    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;

    public SubCommandTerritories(PlayerDataManager pdm, TerritoryManager territoryManager) {
        this.pdm = pdm;
        this.territoryManager = territoryManager;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"territories"};
    }

    @Override
    public String getUsage() {
        return "/clan territories";
    }

    @Override
    public String getDescription() {
        return "Показать всю территорию клана (основная + флаги)";
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

        player.sendMessage(ChatColor.GOLD + "===== Территории клана " + clanName + " =====");

        // Основная территория
        int[] mainTerritory = territoryManager.getClanTerritory(clanName);
        if (mainTerritory != null) {
            player.sendMessage(ChatColor.YELLOW + "Основная база: " + ChatColor.WHITE + Arrays.toString(mainTerritory));
        }

        // Все территории (включая флаги)
        List<int[]> allTerritories = territoryManager.getAllClanTerritories(clanName);
        // Удаляем основную, чтобы не дублировать
        if (mainTerritory != null) {
            allTerritories.removeIf(t -> Arrays.equals(t, mainTerritory));
        }

        if (!allTerritories.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Территории флагов:");
            for (int[] territory : allTerritories) {
                player.sendMessage(ChatColor.GRAY + "- " + Arrays.toString(territory));
            }
        } else if (mainTerritory == null) {
            player.sendMessage(ChatColor.YELLOW + "У клана нет ещё ни одной территории.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "У клана нет дополнительных флагов.");
        }

        return true;
    }
}
