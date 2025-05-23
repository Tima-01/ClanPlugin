package org.plugin.clansPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class RemovePlayerCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;

    public RemovePlayerCommand(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /removeplayer <ник>");
            return true;
        }

        String targetName = args[0];
        String targetClan = playerDataManager.getPlayerClan(targetName);

        if (targetClan == null) {
            sender.sendMessage(ChatColor.RED + "Игрок " + targetName + " не состоит в клане.");
            return true;
        }

        boolean isAdmin = sender.hasPermission("clan.admin");

        if (sender instanceof Player player) {
            String senderClan = playerDataManager.getPlayerClan(player.getName());

            // Проверка: если не админ, должен быть лидером того же клана
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

                // ⛔ Защита от удаления самого себя
                if (player.getName().equalsIgnoreCase(targetName)) {
                    player.sendMessage(ChatColor.RED + "Вы не можете удалить самого себя. Передайте лидерство или используйте /clan leave.");
                    return true;
                }
            }
        }

        playerDataManager.removePlayerFromClan(targetName);
        sender.sendMessage(ChatColor.GREEN + "Игрок " + targetName + " удален из клана " + targetClan + ".");

        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(ChatColor.YELLOW + "Вы были удалены из клана " + targetClan + ".");
        }

        return true;
    }
}
