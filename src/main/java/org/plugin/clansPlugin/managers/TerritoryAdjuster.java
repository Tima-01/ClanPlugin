package org.plugin.clansPlugin.managers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.List;

public class TerritoryAdjuster {

    private final PlayerDataManager playerDataManager;
    private final TerritoryManager territoryManager;

    public TerritoryAdjuster(PlayerDataManager playerDataManager, TerritoryManager territoryManager) {
        this.playerDataManager = playerDataManager;
        this.territoryManager = territoryManager;
    }

    /**
     * Обновляет территорию клана в зависимости от текущего количества участников.
     * @param clanName имя клана
     */
    public void adjustTerritory(String clanName) {
        List<String> members = playerDataManager.getClanMembers(clanName);

        if (members == null || members.isEmpty()) {
            territoryManager.deleteClanTerritory(clanName);
            return;
        }

        // Определяем новую сторону территории
        int newSide = Math.max(4, (int) Math.sqrt(members.size() * 2) + 2);

        // Получаем текущую территорию
        int[] currentTerritory = territoryManager.getClanTerritory(clanName);
        if (currentTerritory == null) return;

        // Центр территории по чанкам
        int centerX = (currentTerritory[0] + currentTerritory[2]) / 2;
        int centerZ = (currentTerritory[1] + currentTerritory[3]) / 2;

        // Преобразуем в координаты блоков
        World world = Bukkit.getWorlds().get(0); // Можно заменить на нужный мир
        Location center = new Location(world, centerX << 4, 0, centerZ << 4);

        // Обновляем территорию
        territoryManager.deleteClanTerritory(clanName);
        territoryManager.createSquareTerritory(clanName, center, newSide);
    }
}
