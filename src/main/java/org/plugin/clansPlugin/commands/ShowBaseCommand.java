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

        int size = 96; // 6 чанков = 96 блоков
        int half = size / 2;
        World world = center.getWorld();
        int y = center.getBlockY() + 1;

        Map<Location, BlockData> changedBlocks = new HashMap<>();

        // Проходимся по периметру
        for (int i = -half; i <= half; i++) {
            // Север и юг
            Location north = new Location(world, center.getX() + i, y, center.getZ() - half);
            Location south = new Location(world, center.getX() + i, y, center.getZ() + half);

            // Запад и восток
            Location west = new Location(world, center.getX() - half, y, center.getZ() + i);
            Location east = new Location(world, center.getX() + half, y, center.getZ() + i);

            for (Location loc : new Location[]{north, south, west, east}) {
                if (loc.getBlock().getType() == Material.AIR) {
                    changedBlocks.put(loc, loc.getBlock().getBlockData());
                    player.sendBlockChange(loc, Material.LIGHT_BLUE_STAINED_GLASS.createBlockData());
                }
            }
        }

        // Убираем через 3 секунды
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, BlockData> entry : changedBlocks.entrySet()) {
                    player.sendBlockChange(entry.getKey(), entry.getValue());
                }
            }
        }.runTaskLater(ClansPlugin.getInstance(), 60L); // 60 тиков = 3 секунды

        player.sendMessage(ChatColor.GREEN + "Границы базы визуализированы стеклом на 3 секунды.");
        return true;
    }
}
