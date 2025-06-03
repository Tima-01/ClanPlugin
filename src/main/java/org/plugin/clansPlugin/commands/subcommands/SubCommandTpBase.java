package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TeleportManager;

public class SubCommandTpBase implements SubCommand {

    private final PlayerDataManager playerDataManager;
    private final TeleportManager teleportManager;

    public SubCommandTpBase(PlayerDataManager pdm, TeleportManager tm) {
        this.playerDataManager = pdm;
        this.teleportManager = tm;
    }

    @Override
    public String getUsage() {
        return "/clan tpbase";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"tpbase"};
    }

    @Override
    public boolean execute(Player player, String[] args) {
        String playerName = player.getName();

        if (!playerDataManager.isPlayerInClan(playerName)) {
            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
            return true;
        }

        // Проверка разрешения на tpbase
        if (!playerDataManager.hasPermission(playerName, "tpbase")) {
            player.sendMessage(ChatColor.RED + "У тебя нет доступа к этой команде. Попроси лидера выдать разрешение.");
            return true;
        }

        String clanName = playerDataManager.getPlayerClan(playerName);
        Location basePoint = teleportManager.getTeleportPoint(clanName);
        if (basePoint == null) {
            player.sendMessage(ChatColor.RED + "Точка телепортации клана не установлена.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Телепортация на базу клана...");
        player.teleport(basePoint);

        return true;
    }

    @Override
    public String getDescription() {
        return "Телепортация на базу клана";
    }
}
