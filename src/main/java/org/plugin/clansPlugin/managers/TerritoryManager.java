package org.plugin.clansPlugin.managers;


import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TerritoryManager {

    private final JavaPlugin plugin;
    private File territoryFile;
    private YamlConfiguration territoryData;

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
                // если новая базa в радиусе 6 чанков от уже существующей
                if (Math.abs(ox - centerChunkX) < 6 && Math.abs(oz - centerChunkZ) < 6) {
                    return true;
                }
            }
        }
        return false;
    }
}
