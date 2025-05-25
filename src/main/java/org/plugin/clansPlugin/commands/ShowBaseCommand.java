package org.plugin.clansPlugin.commands;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.HashMap;
import java.util.Map;

public class ShowBaseCommand implements CommandExecutor {

    private final TerritoryManager territoryManager;
    private final PlayerDataManager playerDataManager;

    public ShowBaseCommand(TerritoryManager territoryManager, PlayerDataManager playerDataManager) {
        this.territoryManager = territoryManager;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        // Получаем клан игрока
        String clanName = playerDataManager.getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(ChatColor.RED + "Ты не в клане.");
            return true;
        }

        // Получаем центр базы
        Location center = territoryManager.getClanBaseCenter(clanName);
        if (center == null) {
            player.sendMessage(ChatColor.RED + "У твоего клана нет базы.");
            return true;
        }

        // Получаем количество участников клана
        int memberCount = playerDataManager.getClanMembers(clanName).size();

        // Вычисляем длину стороны в чанках и переводим в блоки
        int baseSideLength = 6;
        int membersPerExpansion = 1;
        int sideLengthChunks = baseSideLength + (memberCount / membersPerExpansion);
        int sizeBlocks = sideLengthChunks * 16;
        int half = sizeBlocks / 2;

        World world = center.getWorld();
        int y = center.getBlockY() + 1;

        Map<Location, BlockData> changedBlocks = new HashMap<>();

        // Проходимся по периметру
        for (int i = -half; i <= half; i++) {
            Location north = new Location(world, center.getBlockX() + i, y, center.getBlockZ() - half);
            Location south = new Location(world, center.getBlockX() + i, y, center.getBlockZ() + half);
            Location west  = new Location(world, center.getBlockX() - half, y, center.getBlockZ() + i);
            Location east  = new Location(world, center.getBlockX() + half, y, center.getBlockZ() + i);

            for (Location loc : new Location[]{north, south, west, east}) {
                if (loc.getBlock().getType() == Material.AIR) {
                    changedBlocks.put(loc, loc.getBlock().getBlockData());
                    player.sendBlockChange(loc, Material.LIGHT_BLUE_STAINED_GLASS.createBlockData());
                }
            }
        }

        // Убираем границу через 3 секунды
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, BlockData> entry : changedBlocks.entrySet()) {
                    player.sendBlockChange(entry.getKey(), entry.getValue());
                }
            }
        }.runTaskLater(ClansPlugin.getInstance(), 60L);

        player.sendMessage(ChatColor.GREEN + "Границы базы визуализированы стеклом на 3 секунды.");
        return true;
    }
}
