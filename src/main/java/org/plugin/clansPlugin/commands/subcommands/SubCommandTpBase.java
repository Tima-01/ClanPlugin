package org.plugin.clansPlugin.commands.subcommands;


import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class SubCommandTpBase implements SubCommand {

    private final PlayerDataManager playerDataManager;
    private final TerritoryManager territoryManager;

    public SubCommandTpBase(PlayerDataManager pdm, TerritoryManager tm) {
        this.playerDataManager = pdm;
        this.territoryManager = tm;
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

        String clanName = playerDataManager.getPlayerClan(playerName);
        Location baseCenter = territoryManager.getClanBaseCenter(clanName);

        if (baseCenter == null) {
            player.sendMessage(ChatColor.RED + "У вашего клана нет базы.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Телепортация на базу клана...");
        player.teleport(baseCenter);
        return true;
    }


    @Override
    public String getDescription() {
        return "Телепортация на базу клана";
    }
}

