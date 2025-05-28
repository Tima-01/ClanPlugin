package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class SubCommandRemoveFlag implements SubCommand {

    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;

    public SubCommandRemoveFlag(PlayerDataManager pdm, TerritoryManager territoryManager) {
        this.pdm = pdm;
        this.territoryManager = territoryManager;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"removeflag"};
    }

    @Override
    public String getUsage() {
        return "/clan removeflag";
    }

    @Override
    public String getDescription() {
        return "Удалить флаг клана (только лидер)";
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
            player.sendMessage(ChatColor.RED + "Только лидер клана может удалять флаги.");
            return true;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || !(targetBlock.getState() instanceof Banner)) {
            player.sendMessage(ChatColor.RED + "Посмотрите на флаг, который хотите удалить.");
            return true;
        }

        Location flagLocation = targetBlock.getLocation();
        boolean removed = territoryManager.removeClanFlag(flagLocation);
        if (removed) {
            // Удаляем сам баннер
            targetBlock.setType(Material.AIR);
            player.sendMessage(ChatColor.GREEN + "Флаг успешно удален!");
        } else {
            player.sendMessage(ChatColor.RED + "Это не флаг твоего клана или произошла ошибка.");
        }
        return true;
    }
}
