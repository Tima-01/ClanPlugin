package org.plugin.clansPlugin.managers;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TerritoryManager {

    private final JavaPlugin plugin;
    private File territoryFile;
    private YamlConfiguration territoryData;

    public TerritoryManager(JavaPlugin plugin) {
        this.plugin = plugin;
        initTerritoryFile();
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

    public void saveTerritoryData() {
        try {
            territoryData.save(territoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Сохраняет территорию клана в виде квадрата (4 угловые точки)
     * Формат: minX,minZ,maxX,maxZ
     */
    public void setClanTerritory(String clanName, int minX, int minZ, int maxX, int maxZ) {
        territoryData.set("territories." + clanName, minX + "," + minZ + "," + maxX + "," + maxZ);
        saveTerritoryData();
    }

    /**
     * Получает границы территории клана
     * @return массив [minX, minZ, maxX, maxZ] или null если нет территории
     */
    public int[] getClanTerritory(String clanName) {
        if (!territoryData.contains("territories." + clanName)) {
            return null;
        }

        String[] coords = territoryData.getString("territories." + clanName).split(",");
        return new int[]{
                Integer.parseInt(coords[0]),
                Integer.parseInt(coords[1]),
                Integer.parseInt(coords[2]),
                Integer.parseInt(coords[3])
        };
    }

    /**
     * Проверяет, находится ли чанк на территории клана
     */
    public boolean isInTerritory(String clanName, int chunkX, int chunkZ) {
        int[] territory = getClanTerritory(clanName);
        if (territory == null) return false;

        return chunkX >= territory[0] &&
                chunkX <= territory[2] &&
                chunkZ >= territory[1] &&
                chunkZ <= territory[3];
    }

    /**
     * Получает клан, которому принадлежит чанк
     */
    public String getClanByChunk(int chunkX, int chunkZ) {
        // Сначала проверяем основные территории
        String clan = getClanByMainTerritory(chunkX, chunkZ);
        if (clan != null) return clan;

        // Затем проверяем территории флагов
        if (territoryData.contains("flags")) {
            for (String clanName : territoryData.getConfigurationSection("flags").getKeys(false)) {
                for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
                    String[] data = territoryData.getString("flags." + clanName + "." + flagId).split(",");
                    if (data.length >= 4) {
                        int minX = Integer.parseInt(data[0]);
                        int minZ = Integer.parseInt(data[1]);
                        int maxX = Integer.parseInt(data[2]);
                        int maxZ = Integer.parseInt(data[3]);

                        if (chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ) {
                            return clanName;
                        }
                    }
                }
            }
        }

        return null;
    }

    private String getClanByMainTerritory(int chunkX, int chunkZ) {
        if (!territoryData.contains("territories")) return null;

        for (String clanName : territoryData.getConfigurationSection("territories").getKeys(false)) {
            if (isInTerritory(clanName, chunkX, chunkZ)) {
                return clanName;
            }
        }
        return null;
    }

    /**
     * Рассчитывает границы территории на основе центра и количества чанков
     */
    public void createSquareTerritory(String clanName, Location center, int sideLength) {
        int centerX = center.getBlockX() >> 4;
        int centerZ = center.getBlockZ() >> 4;

        int radius = sideLength / 2;
        int minX = centerX - radius;
        int minZ = centerZ - radius;
        int maxX = centerX + radius;
        int maxZ = centerZ + radius;

        setClanTerritory(clanName, minX, minZ, maxX, maxZ);
    }

    /**
     * Удаляет территорию клана
     */
    public void deleteClanTerritory(String clanName) {
        territoryData.set("territories." + clanName, null);
        saveTerritoryData();
    }

    /**
     * Проверяет пересечение территорий
     */
    public boolean isOverlapping(int centerX, int centerZ, int sideLength) {
        return isOverlapping(centerX, centerZ, sideLength, null);
    }

    /**
     * Проверяет пересечение территорий (с исключением определенного клана)
     */
    public boolean isOverlapping(int centerX, int centerZ, int sideLength, String excludeClan) {
        if (!territoryData.contains("territories")) return false;

        int radius = sideLength / 2;
        int newMinX = centerX - radius;
        int newMinZ = centerZ - radius;
        int newMaxX = centerX + radius;
        int newMaxZ = centerZ + radius;

        for (String clanName : territoryData.getConfigurationSection("territories").getKeys(false)) {
            if (clanName.equals(excludeClan)) continue;

            int[] territory = getClanTerritory(clanName);
            if (territory == null) continue;

            // Проверка пересечения прямоугольников
            if (newMaxX >= territory[0] && newMinX <= territory[2] &&
                    newMaxZ >= territory[1] && newMinZ <= territory[3]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Получает центр территории клана
     */
    public Location getClanBaseCenter(String clanName) {
        int[] territory = getClanTerritory(clanName);
        if (territory == null) return null;

        int centerX = (territory[0] + territory[2]) / 2;
        int centerZ = (territory[1] + territory[3]) / 2;

        World world = Bukkit.getWorlds().get(0);
        int blockX = centerX * 16 + 8;
        int blockZ = centerZ * 16 + 8;
        int blockY = world.getHighestBlockYAt(blockX, blockZ);

        return new Location(world, blockX, blockY, blockZ);
    }

    public void addClanFlag(String clanName, Location flagLocation) {
        String flagKey = "flags." + clanName + "." + UUID.randomUUID().toString();
        territoryData.set(flagKey + ".world", flagLocation.getWorld().getName());
        territoryData.set(flagKey + ".x", flagLocation.getBlockX());
        territoryData.set(flagKey + ".y", flagLocation.getBlockY());
        territoryData.set(flagKey + ".z", flagLocation.getBlockZ());
        saveTerritoryData();
    }

    public List<Location> getClanFlags(String clanName) {
        List<Location> flags = new ArrayList<>();
        if (!territoryData.contains("flags." + clanName)) {
            return flags;
        }

        for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
            String[] data = territoryData.getString("flags." + clanName + "." + flagId).split(",");
            if (data.length == 7) {
                World world = Bukkit.getWorlds().get(0); // Получаем основной мир
                int x = Integer.parseInt(data[4]);
                int y = Integer.parseInt(data[5]);
                int z = Integer.parseInt(data[6]);
                flags.add(new Location(world, x, y, z));
            }
        }
        return flags;
    }
    /**
     * Пытается добавить флаг (3x3 чанка) к территории клана
     * @param flagLocation Местоположение флага (блок)
     * @param clanName Название клана
     * @return true если флаг был успешно добавлен, false если невозможно
     */
    public boolean addFlagTerritory(String clanName, Location flagLocation) {
        int flagChunkX = flagLocation.getBlockX() >> 4;
        int flagChunkZ = flagLocation.getBlockZ() >> 4;
        int flagSize = 3;

        // Координаты территории флага (3x3 чанка)
        int flagMinX = flagChunkX - 1;
        int flagMinZ = flagChunkZ - 1;
        int flagMaxX = flagChunkX + 1;
        int flagMaxZ = flagChunkZ + 1;

        // Проверяем пересечение с другими кланами (кроме своего)
        if (isOverlapping(flagChunkX, flagChunkZ, flagSize, clanName)) {
            return false;
        }

        // Проверяем, что флаг примыкает к территории клана
        if (!isAdjacentToClanTerritory(clanName, flagMinX, flagMinZ, flagMaxX, flagMaxZ)) {
            return false;
        }

        // Сохраняем флаг в компактном формате
        String flagKey = "flags." + clanName + "." + UUID.randomUUID();
        territoryData.set(flagKey,
                flagMinX + "," + flagMinZ + "," + flagMaxX + "," + flagMaxZ + "," +
                        flagLocation.getBlockX() + "," + flagLocation.getBlockY() + "," + flagLocation.getBlockZ());

        saveTerritoryData();
        return true;
    }

    private boolean isAdjacentToClanTerritory(String clanName, int minX, int minZ, int maxX, int maxZ) {
        // Проверяем основную территорию
        int[] mainTerritory = getClanTerritory(clanName);
        if (mainTerritory != null && isAdjacent(mainTerritory, minX, minZ, maxX, maxZ)) {
            return true;
        }

        // Проверяем все флаги клана
        if (territoryData.contains("flags." + clanName)) {
            for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
                String[] coords = territoryData.getString("flags." + clanName + "." + flagId + ".territory").split(",");
                int[] flagTerritory = new int[]{
                        Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]),
                        Integer.parseInt(coords[3])
                };

                if (isAdjacent(flagTerritory, minX, minZ, maxX, maxZ)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isAdjacent(int[] territory1, int minX, int minZ, int maxX, int maxZ) {
        // Проверяем, что территории соприкасаются хотя бы одной стороной
        return (maxX == territory1[0] - 1 || minX == territory1[2] + 1 ||
                maxZ == territory1[1] - 1 || minZ == territory1[3] + 1);
    }

    /**
     * Проверяет, находится ли чанк на территории клана (основной или флагов)
     */
    public boolean isInClanTerritory(String clanName, int chunkX, int chunkZ) {
        // Проверяем основную территорию
        if (isInTerritory(clanName, chunkX, chunkZ)) {
            return true;
        }

        // Проверяем территории флагов
        if (territoryData.contains("flags." + clanName)) {
            for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
                String[] coords = territoryData.getString("flags." + clanName + "." + flagId + ".territory").split(",");
                int[] flagTerritory = new int[]{
                        Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]),
                        Integer.parseInt(coords[3])
                };

                if (chunkX >= flagTerritory[0] && chunkX <= flagTerritory[2] &&
                        chunkZ >= flagTerritory[1] && chunkZ <= flagTerritory[3]) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Удаляет флаг и пересчитывает территорию клана
     */
    public boolean removeClanFlag(Location flagLocation) {
        if (!territoryData.contains("flags")) {
            return false;
        }

        for (String clanName : territoryData.getConfigurationSection("flags").getKeys(false)) {
            ConfigurationSection clanFlags = territoryData.getConfigurationSection("flags." + clanName);
            for (String flagId : clanFlags.getKeys(false)) {
                String[] data = clanFlags.getString(flagId).split(",");
                if (data.length == 7) {
                    int x = Integer.parseInt(data[4]);
                    int y = Integer.parseInt(data[5]);
                    int z = Integer.parseInt(data[6]);

                    if (x == flagLocation.getBlockX() &&
                            y == flagLocation.getBlockY() &&
                            z == flagLocation.getBlockZ()) {

                        territoryData.set("flags." + clanName + "." + flagId, null);
                        saveTerritoryData();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<int[]> getAllClanTerritories(String clanName) {
        List<int[]> territories = new ArrayList<>();

        // Добавляем основную территорию
        int[] mainTerritory = getClanTerritory(clanName);
        if (mainTerritory != null) {
            territories.add(mainTerritory);
        }

        // Добавляем территории флагов
        if (territoryData.contains("flags." + clanName)) {
            for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
                String[] coords = territoryData.getString("flags." + clanName + "." + flagId + ".territory").split(",");
                territories.add(new int[]{
                        Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]),
                        Integer.parseInt(coords[3])
                });
            }
        }

        return territories;
    }

}