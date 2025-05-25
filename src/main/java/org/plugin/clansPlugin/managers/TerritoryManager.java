package org.plugin.clansPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TerritoryManager {

    private final JavaPlugin plugin;
    private File territoryFile;
    private YamlConfiguration territoryData;
    private final java.util.Map<String, Set<Chunk>> territories = new java.util.HashMap<>();

    public TerritoryManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveResource("territories.yml", false);
    }

    public void initTerritoryFile() {
        territoryFile = new File(plugin.getDataFolder(), "territories.yml");
        if (!territoryFile.exists()) {
            try {
                territoryFile.getParentFile().mkdirs();
                territoryFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        territoryData = YamlConfiguration.loadConfiguration(territoryFile);
    }
    public void saveTerritories() {
        for (String clan : territories.keySet()) {
            List<String> chunkList = new java.util.ArrayList<>();
            for (Chunk chunk : territories.get(clan)) {
                chunkList.add(chunk.getX() + "," + chunk.getZ());
            }
            territoryData.set("territories." + clan, chunkList);
        }
        saveTerritoryData();
    }

    public void saveTerritoryData() {
        try {
            territoryData.save(territoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Вернуть список чанков (строк вида "x,z") для заданного клана.
     * Если у клана нет базы — вернуть пустой список.
     */
    public List<String> getClanChunks(String clanName) {
        if (!territoryData.contains("territories." + clanName)) return Collections.emptyList();
        return territoryData.getStringList("territories." + clanName);
    }

    /**
     * Сохранить список чанков для клана (перезаписать существующую базу).
     */
    public void setClanChunks(String clanName, List<String> chunks) {
        territoryData.set("territories." + clanName, chunks);
        saveTerritoryData();
    }

    /**
     * Удалить базу клана.
     */
    public void deleteClanChunks(String clanName) {
        territoryData.set("territories." + clanName, null);
        saveTerritoryData();
    }
    /**
     *
     */
    public YamlConfiguration getTerritoryData() {
        return territoryData;
    }

    /**
     * По координатам чанка (chunkX, chunkZ) найти, на территории какого клана сейчас находится игрок.
     * Если в этом чанке нет ни одной базы — вернуть null.
     */
    public String getClanByChunk(int chunkX, int chunkZ) {
        if (!territoryData.contains("territories")) return null;
        ConfigurationSection sec = territoryData.getConfigurationSection("territories");
        if (sec == null) return null;

        // Перебираем все кланы, у которых есть список чанков
        for (String clanName : sec.getKeys(false)) {
            List<String> chunks = territoryData.getStringList("territories." + clanName);
            for (String coord : chunks) {
                String[] parts = coord.split(",");
                int cX = Integer.parseInt(parts[0]);
                int cZ = Integer.parseInt(parts[1]);
                if (cX == chunkX && cZ == chunkZ) {
                    return clanName;
                }
            }
        }
        return null;
    }

    /**
     * Проверка пересечения территории нового клана с уже созданными.
     * Возвращает true, если база перекрывается с любым другим кланом.
     */
    public boolean isOverlapping(int centerChunkX, int centerChunkZ) {
        if (!territoryData.contains("territories")) return false;
        ConfigurationSection sec = territoryData.getConfigurationSection("territories");
        if (sec == null) return false;

        for (String clanName : sec.getKeys(false)) {
            List<String> chunks = territoryData.getStringList("territories." + clanName);
            for (String coord : chunks) {
                String[] parts = coord.split(",");
                int ox = Integer.parseInt(parts[0]);
                int oz = Integer.parseInt(parts[1]);
                // если новая база в радиусе 6 чанков от уже существующей
                if (Math.abs(ox - centerChunkX) < 6 && Math.abs(oz - centerChunkZ) < 6) {
                    return true;
                }
            }
        }
        return false;
    }
    public Set<Chunk> calculateTerritory(Location center, int chunkCount) {
        Set<Chunk> result = new java.util.HashSet<>();
        if (center == null || center.getWorld() == null) return result;

        int cx = center.getChunk().getX();
        int cz = center.getChunk().getZ();
        World world = center.getWorld();

        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();

        queue.add(new int[]{cx, cz});
        visited.add(cx + "," + cz);

        while (!queue.isEmpty() && result.size() < chunkCount) {
            int[] coords = queue.poll();
            int x = coords[0];
            int z = coords[1];

            Chunk chunk = world.getChunkAt(x, z);
            result.add(chunk);

            // Добавляем соседние чанки (в 4 стороны)
            int[][] directions = {
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            };

            for (int[] dir : directions) {
                int nx = x + dir[0];
                int nz = z + dir[1];
                String key = nx + "," + nz;
                if (!visited.contains(key)) {
                    visited.add(key);
                    queue.add(new int[]{nx, nz});
                }
            }
        }

        return result;
    }

    public void adjustClanTerritorySize(String clanName, int memberCount) {
        int baseSideLength = 6; // начальная сторона квадрата
        int membersPerExpansion = 1; // сколько участников нужно для расширения на +1
        int sideLength = baseSideLength + (memberCount / membersPerExpansion);

        Location center = getClanBaseCenter(clanName);
        if (center == null) return;

        List<String> newChunks = new ArrayList<>();
        int centerChunkX = center.getBlockX() >> 4;
        int centerChunkZ = center.getBlockZ() >> 4;

        int radius = sideLength / 2;
        int startX = centerChunkX - radius;
        int startZ = centerChunkZ - radius;
        int endX = startX + sideLength - 1;
        int endZ = startZ + sideLength - 1;

        for (int dx = startX; dx <= endX; dx++) {
            for (int dz = startZ; dz <= endZ; dz++) {
                newChunks.add(dx + "," + dz);
            }
        }

        setClanChunks(clanName, newChunks);
    }


    /**
     * Возвращает центр базы клана в виде Location.
     * Если у клана нет сохранённых чанков, возвращает null.
     *
     * Логика:
     * 1) Считываем список строк "chunkX,chunkZ" для данного clanName.
     * 2) Находим минимальные и максимальные значения chunkX и chunkZ среди всех чанков базы.
     * 3) Рассчитываем центральный чанк: (minX + maxX) / 2, (minZ + maxZ) / 2.
     * 4) Переводим координаты "центр чанка" в координаты блока: centerChunkX * 16 + 8, centerChunkZ * 16 + 8.
     * 5) Определяем высоту Y, используя самый высокий блок на этой X,Z через world.getHighestBlockYAt().
     * 6) Берём первый доступный мир сервера (если у вас лишь один мир — этого достаточно).
     */
    public Location getClanBaseCenter(String clanName) {
        if (!territoryData.contains("territories." + clanName)) {
            return null;
        }

        List<String> chunks = territoryData.getStringList("territories." + clanName);
        if (chunks.isEmpty()) {
            return null;
        }

        int minChunkX = Integer.MAX_VALUE;
        int maxChunkX = Integer.MIN_VALUE;
        int minChunkZ = Integer.MAX_VALUE;
        int maxChunkZ = Integer.MIN_VALUE;

        for (String coord : chunks) {
            String[] parts = coord.split(",");
            int cX = Integer.parseInt(parts[0].trim());
            int cZ = Integer.parseInt(parts[1].trim());

            if (cX < minChunkX) minChunkX = cX;
            if (cX > maxChunkX) maxChunkX = cX;
            if (cZ < minChunkZ) minChunkZ = cZ;
            if (cZ > maxChunkZ) maxChunkZ = cZ;
        }

        int centerChunkX = (minChunkX + maxChunkX) / 2;
        int centerChunkZ = (minChunkZ + maxChunkZ) / 2;

        int blockX = centerChunkX * 16 + 8;
        int blockZ = centerChunkZ * 16 + 8;

        World world = Bukkit.getServer().getWorlds().get(0);
        if (world == null) {
            return null;
        }

        int blockY = world.getHighestBlockYAt(blockX, blockZ);

        return new Location(world, blockX, blockY, blockZ);
    }

    // --- Новые методы ниже ---

    /**
     * Возвращает множество кланов, у которых есть базы (территории).
     */
    public Set<String> getAllClanBases() {
        if (!territoryData.contains("territories")) {
            return Collections.emptySet();
        }
        return territoryData.getConfigurationSection("territories").getKeys(false);
    }

    /**
     * Возвращает локацию базы клана (центр базы), или null, если база не найдена.
     * В данном случае просто делегируем вызов getClanBaseCenter.
     */
    public Location getBaseLocation(String clanName) {
        return getClanBaseCenter(clanName);
    }
}
