package org.plugin.clansPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

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

        // Визуализация границ базы
        for (int i = -half; i <= half; i += 4) {
            world.spawnParticle(Particle.FLAME, center.getX() + i, y, center.getZ() - half, 1);
            world.spawnParticle(Particle.FLAME, center.getX() + i, y, center.getZ() + half, 1);
            world.spawnParticle(Particle.FLAME, center.getX() - half, y, center.getZ() + i, 1);
            world.spawnParticle(Particle.FLAME, center.getX() + half, y, center.getZ() + i, 1);
        }

        player.sendMessage(ChatColor.GREEN + "Границы базы визуализированы на несколько секунд.");
        return true;
    }
}
