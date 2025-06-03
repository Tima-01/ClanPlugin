package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.Arrays;
import java.util.List;

public class SubCommandTerritories implements SubCommand {

    private final PlayerDataManager playerDataManager;
    private final TerritoryManager territoryManager;

    public SubCommandTerritories(PlayerDataManager pdm, TerritoryManager territoryManager) {
        this.playerDataManager = pdm;
        this.territoryManager = territoryManager;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"territories", "terr"};
    }

    @Override
    public String getUsage() {
        return "/clan territories";
    }

    @Override
    public String getDescription() {
        return "Показать всю территорию клана (основная + флаги) в обычных координатах";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String playerName = player.getName();
        String clanName = playerDataManager.getPlayerClan(playerName);
        if (clanName == null) {
            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
            return true;
        }

        if (!playerDataManager.isClanLeader(playerName) &&
                !playerDataManager.hasTrust(playerName)) {
            player.sendMessage(ChatColor.RED + "Лидер вам не доверяет.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "===== Территории клана " + clanName + " =====");

        // Основная территория
        int[] mainTerritory = territoryManager.getClanTerritory(clanName);
        if (mainTerritory != null) {
            String formatted = formatTerritory(mainTerritory);
            player.sendMessage(ChatColor.YELLOW + "Основная база: " + ChatColor.WHITE + formatted);
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
                String formatted = formatTerritory(territory);
                player.sendMessage(ChatColor.GRAY + "- " + formatted);
            }
        } else if (mainTerritory == null) {
            player.sendMessage(ChatColor.YELLOW + "У клана нет ещё ни одной территории.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "У клана нет дополнительных флагов.");
        }

        return true;
    }

    private String formatTerritory(int[] territory) {
        // Предполагаем формат территории: [x1, z1, x2, z2, world, centerX, centerY, centerZ, radius]
        if (territory.length >= 9) {
            int centerX = territory[5];
            int centerY = territory[6];
            int centerZ = territory[7];
            return String.format("Центр: %d, %d, %d", centerX, centerY, centerZ);
        }
        // Если формат другой, вернем как есть
        return Arrays.toString(territory);
    }
}