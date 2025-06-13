package org.plugin.clansPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryAdjuster;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class RemovePlayerCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;
    private final TerritoryManager territoryManager;

    public RemovePlayerCommand(PlayerDataManager playerDataManager, TerritoryManager territoryManager) {
        this.playerDataManager = playerDataManager;
        this.territoryManager = territoryManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверяем количество аргументов
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /removenigga <ник>");
            return true;
        }

        String targetName = args[0];
        String targetClan = playerDataManager.getPlayerClan(targetName);

        // Если игрок не в клане
        if (targetClan == null) {
            sender.sendMessage(ChatColor.RED + "Игрок " + targetName + " не состоит в клане.");
            return true;
        }

        // Получаем текущее имя лидера клана (до удаления)
        String currentLeader = playerDataManager.getClanLeader(targetClan);

        boolean isAdmin = sender.hasPermission("clan.admin");

        if (sender instanceof Player player) {
            String senderClan = playerDataManager.getPlayerClan(player.getName());

            // Если отправитель — не админ, он должен быть лидером своего клана
            if (!isAdmin) {
                if (!targetClan.equals(senderClan)) {
                    player.sendMessage(ChatColor.RED + "Вы не можете удалить игрока из другого клана.");
                    return true;
                }

                String leader = playerDataManager.getClanLeader(senderClan);
                if (!player.getName().equalsIgnoreCase(leader)) {
                    player.sendMessage(ChatColor.RED + "Только лидер может удалять участников клана.");
                    return true;
                }

                // Защита от удаления самого себя
                if (player.getName().equalsIgnoreCase(targetName)) {
                    player.sendMessage(ChatColor.RED + "Вы не можете удалить самого себя. Передайте лидерство или используйте /clan leave.");
                    return true;
                }
            }
        }

        // Удаляем игрока из клана
        playerDataManager.removePlayerFromClan(targetName);
        sender.sendMessage(ChatColor.GREEN + "Игрок " + targetName + " удалён из клана " + targetClan + ".");

        // Если удалённый был лидером, сбрасываем флаг лидера
        if (targetName.equalsIgnoreCase(currentLeader)) {
            playerDataManager.setClanLeader(targetClan, null);
            sender.sendMessage(ChatColor.RED + "У клана " + targetClan + " больше нет лидера.");
            Player removedPlayer = Bukkit.getPlayerExact(targetName);
            if (removedPlayer != null && removedPlayer.isOnline()) {
                removedPlayer.sendMessage(ChatColor.RED + "Вы были лидером клана " + targetClan + ". Лидерство снято.");
            }
        }

        // Уведомляем самого удаляемого, если он онлайн
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(ChatColor.YELLOW + "Вы были удалены из клана " + targetClan + ".");
        }

        // === СЖАТИЕ (уменьшение) ТЕРРИТОРИИ ПОСЛЕ ИСКЛЮЧЕНИЯ ===
        TerritoryAdjuster adjuster = new TerritoryAdjuster(playerDataManager, territoryManager);
        adjuster.adjustTerritory(targetClan);

        return true;
    }
}
