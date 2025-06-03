package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryAdjuster;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class SubCommandRemovePlayer implements SubCommand {

    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;

    public SubCommandRemovePlayer(PlayerDataManager pdm, TerritoryManager territoryManager) {
        this.pdm = pdm;
        this.territoryManager = territoryManager;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"removeplayer"};
    }

    @Override
    public String getUsage() {
        return "/clan removeplayer <ник>";
    }

    @Override
    public String getDescription() {
        return "Удалить игрока из вашего клана (только для лидера)";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String senderName = player.getName();
        String targetName = args[0];

        // Проверка, состоит ли отправитель в клане
        if (!pdm.isPlayerInClan(senderName)) {
            player.sendMessage(ChatColor.RED + "Вы не состоите в клане.");
            return true;
        }

        String clanName = pdm.getPlayerClan(senderName);
        String leaderName = pdm.getClanLeader(clanName);

        // Проверка, является ли отправитель лидером
        if (!senderName.equalsIgnoreCase(leaderName)) {
            player.sendMessage(ChatColor.RED + "Только лидер может удалять игроков из клана.");
            return true;
        }

        // Проверка, в том ли клане целевой игрок
        String targetClan = pdm.getPlayerClan(targetName);
        if (targetClan == null || !targetClan.equals(clanName)) {
            player.sendMessage(ChatColor.RED + "Игрок " + targetName + " не состоит в вашем клане.");
            return true;
        }

        // Нельзя удалить самого себя
        if (senderName.equalsIgnoreCase(targetName)) {
            player.sendMessage(ChatColor.RED + "Вы не можете удалить самого себя. Используйте /clan leave.");
            return true;
        }

        // Удаление игрока из клана
        pdm.removePlayerFromClan(targetName);
        pdm.savePlayerData();

        player.sendMessage(ChatColor.GREEN + "Игрок " + targetName + " был удалён из клана.");

        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(ChatColor.RED + "Вы были удалены из клана " + clanName + ".");
        }

        // Перерасчёт территории
        TerritoryAdjuster adjuster = new TerritoryAdjuster(pdm, territoryManager);
        adjuster.adjustTerritory(clanName);

        return true;
    }
}
