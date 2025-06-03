package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TeleportManager;

public class SubCommandSetBase implements SubCommand {

    private final PlayerDataManager playerDataManager;
    private final TeleportManager teleportManager;

    public SubCommandSetBase(ClansPlugin plugin) {
        this.playerDataManager = plugin.getPlayerDataManager();
        this.teleportManager = plugin.getTeleportManager();
    }

    @Override
    public String[] getAliases() {
        return new String[]{"setbase"};
    }

    @Override
    public String getUsage() {
        return "/clan setbase";
    }

    @Override
    public String getDescription() {
        return "Установить точку базы для телепортации клана";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        String playerName = player.getName();

        if (!playerDataManager.isPlayerInClan(playerName)) {
            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
            return true;
        }

        String clanName = playerDataManager.getPlayerClan(playerName);
        String leaderName = playerDataManager.getClanLeader(clanName);

        if (!playerName.equalsIgnoreCase(leaderName)) {
            player.sendMessage(ChatColor.RED + "Только лидер клана может установить точку базы.");
            return true;
        }

        Location location = player.getLocation();
        teleportManager.setTeleportPoint(clanName, location);
        player.sendMessage(ChatColor.GREEN + "Точка телепортации клана успешно установлена.");

        return true;
    }
}
