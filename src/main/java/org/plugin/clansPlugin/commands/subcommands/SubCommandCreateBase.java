package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class SubCommandCreateBase implements SubCommand {

    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;

    public SubCommandCreateBase(PlayerDataManager pdm, TerritoryManager territoryManager) {
        this.pdm = pdm;
        this.territoryManager = territoryManager;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"createbase"};
    }

    @Override
    public String getUsage() {
        return "/clan createbase";
    }

    @Override
    public String getDescription() {
        return "Создать основную базу клана (только лидер)";
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
            player.sendMessage(ChatColor.RED + "Только лидер может создавать базу.");
            return true;
        }

        if (territoryManager.getClanTerritory(clanName) != null) {
            player.sendMessage(ChatColor.RED + "У этого клана уже есть база.");
            return true;
        }

        // Фиксированная точка спавна
        Location basePoint = new Location(player.getWorld(), 1340, 68, 300);
        Location loc = player.getLocation();
        if (loc.distance(basePoint) < 1000) {
            player.sendMessage(ChatColor.RED + "База должна быть не ближе 1000 блоков от точки (1340,68,300).");
            return true;
        }

        int chunkX = loc.getChunk().getX();
        int chunkZ = loc.getChunk().getZ();
        int territorySize = 6; // 6×6 чанков

        // Проверка пересечения
        if (territoryManager.isOverlapping(chunkX, chunkZ, territorySize)) {
            player.sendMessage(ChatColor.RED + "База слишком близко к базе другого клана.");
            return true;
        }

        // Создаём территорию
        territoryManager.createSquareTerritory(clanName, loc, territorySize);
        player.sendMessage(ChatColor.GREEN + "База клана успешно установлена!");
        return true;
    }
}
