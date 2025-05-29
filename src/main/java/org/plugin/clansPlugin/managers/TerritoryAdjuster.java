package org.plugin.clansPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.List;

public class TerritoryAdjuster {
    private final PlayerDataManager playerDataManager;
    private final TerritoryManager territoryManager;

    // Должно совпадать с SubCommandCreateBase
    private static final int BASE_TERRITORY_SIZE = 5;  // 5x5 чанков (начальный размер)
    private static final int SIZE_PER_MEMBER = 2;       // +2 чанка за каждого участника (по 1 в каждую сторону)
    private static final int MAX_TERRITORY_SIZE = 15;   // Максимум 15x15

    public TerritoryAdjuster(PlayerDataManager playerDataManager, TerritoryManager territoryManager) {
        this.playerDataManager = playerDataManager;
        this.territoryManager = territoryManager;
    }

    public void adjustTerritory(String clanName) {
        List<String> members = playerDataManager.getClanMembers(clanName);
        if (members == null || members.isEmpty()) {
            territoryManager.deleteClanTerritory(clanName);
            return;
        }

        int[] currentTerritory = territoryManager.getClanTerritory(clanName);
        if (currentTerritory == null) return;

        // Рассчитываем новый размер
        int newSide = calculateTerritorySize(members.size());

        // Проверяем, нужно ли обновлять территорию
        int currentSide = currentTerritory[2] - currentTerritory[0] + 1;
        if (newSide == currentSide) {
            return; // Размер не изменился
        }

        // Центр территории
        int centerX = (currentTerritory[0] + currentTerritory[2]) / 2;
        int centerZ = (currentTerritory[1] + currentTerritory[3]) / 2;

        World world = Bukkit.getWorlds().get(0);
        Location center = new Location(world, centerX << 4, 0, centerZ << 4);

        // Обновляем территорию
        territoryManager.deleteClanTerritory(clanName);
        territoryManager.createSquareTerritory(clanName, center, newSide);

        Bukkit.getLogger().info("Территория клана " + clanName +
                " расширена до " + newSide + "x" + newSide +
                " (участников: " + members.size() + ")");
    }

    private int calculateTerritorySize(int memberCount) {
        // Базовый размер + по 1 чанку в каждую сторону за каждого участника
        int size = BASE_TERRITORY_SIZE + (memberCount * SIZE_PER_MEMBER);
        return Math.min(size, MAX_TERRITORY_SIZE);
    }
}