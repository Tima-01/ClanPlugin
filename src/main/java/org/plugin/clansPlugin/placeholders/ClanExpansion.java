package org.plugin.clansPlugin.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.plugin.clansPlugin.ClansPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClanExpansion extends PlaceholderExpansion {

    private final ClansPlugin plugin;
    private final File dataFolder;

    private FileConfiguration playerConfig;
    private FileConfiguration territoryConfig;

    // Кеш: ключ — игрок+идентификатор, значение — CacheEntry с результатом и временем обновления
    private final Map<String, CacheEntry> cache = new HashMap<>();

    // Время кеширования в миллисекундах (15 минут)
    private static final long CACHE_TIME = 60 * 60 * 1000;

    public ClanExpansion(ClansPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();

        loadConfigs();
    }

    private void loadConfigs() {
        try {
            File playerFile = new File(dataFolder, "players.yml");
            if (!playerFile.exists()) {
                plugin.getLogger().warning("Файл player.yml не найден!");
                playerConfig = null;
            } else {
                playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                plugin.getLogger().info("players.yml загружен успешно.");
            }

            File territoryFile = new File(dataFolder, "territories.yml");
            if (!territoryFile.exists()) {
                plugin.getLogger().warning("Файл territories.yml не найден!");
                territoryConfig = null;
            } else {
                territoryConfig = YamlConfiguration.loadConfiguration(territoryFile);
                plugin.getLogger().info("territories.yml загружен успешно.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка загрузки конфигурационных файлов:");
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clan";
    }

    @Override
    public String getAuthor() {
        return "Ты";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return null;

        String key = player.getName().toLowerCase() + ":" + identifier.toLowerCase();

        // Проверяем кеш
        CacheEntry cached = cache.get(key);
        long now = System.currentTimeMillis();

        if (cached != null && (now - cached.timestamp) < CACHE_TIME) {
            // Возвращаем кешированное значение
            return cached.value;
        }

        // Если нет в кеше или кеш устарел — обновляем
        String playerName = player.getName();
        plugin.getLogger().info("Запрос плейсхолдера '" + identifier + "' для игрока " + playerName);

        String clanName = getPlayerClan(playerName);
        plugin.getLogger().info("Клан игрока " + playerName + ": " + clanName);

        String result;

        if (clanName == null) {
            plugin.getLogger().info("Клан не найден у игрока " + playerName);
            result = "Пока нет";
        } else {
            switch (identifier.toLowerCase()) {
                case "tag":
                    result = " [" + clanName + "]";
                    break;
                case "leader":
                    String leaderName = getClanLeader(clanName);
                    plugin.getLogger().info("Лидер клана " + clanName + ": " + leaderName);
                    result = leaderName != null ? leaderName : "Нет лидера";
                    break;
                case "members":
                    List<String> members = getClanMembers(clanName);
                    plugin.getLogger().info("Количество участников в клане " + clanName + ": " + (members != null ? members.size() : 0));
                    result = members != null ? String.valueOf(members.size()) : "0";
                    break;
                case "territory":
                    List<String> chunks = getClanTerritory(clanName);
                    plugin.getLogger().info("Количество чанков территории клана " + clanName + ": " + (chunks != null ? chunks.size() : 0));
                    result = chunks != null ? String.valueOf(chunks.size()) : "0";
                    break;
                case "base":
                    if (plugin instanceof ClansPlugin) {
                        Location base = ((ClansPlugin) plugin).getTerritoryManager().getClanBaseCenter(clanName);
                        if (base != null) {
                            result = String.format("X: %d, Y: %d, Z: %d", base.getBlockX(), base.getBlockY(), base.getBlockZ());
                        } else {
                            result = "Пока нет";
                        }
                    } else {
                        result = "Ошибка плагина";
                    }
                    break;
                default:
                    result = null;
            }
        }

        // Запоминаем в кеш
        if (result != null) {
            cache.put(key, new CacheEntry(result, now));
        }

        return result;
    }

    private String getPlayerClan(String playerName) {
        if (playerConfig == null) {
            plugin.getLogger().warning("playerConfig не загружен!");
            return null;
        }
        String clan = playerConfig.getString("players." + playerName.toLowerCase());
        plugin.getLogger().info("getPlayerClan: игрок " + playerName + ", клан: " + clan);
        return clan;
    }

    private String getClanLeader(String clanName) {
        if (playerConfig == null) {
            plugin.getLogger().warning("playerConfig не загружен!");
            return null;
        }
        String leader = playerConfig.getString("leaders." + clanName);
        plugin.getLogger().info("getClanLeader: клан " + clanName + ", лидер: " + leader);
        return leader;
    }

    private List<String> getClanMembers(String clanName) {
        if (playerConfig == null) {
            plugin.getLogger().warning("playerConfig не загружен!");
            return Collections.emptyList();
        }

        List<String> members = new ArrayList<>();
        ConfigurationSection playersSection = playerConfig.getConfigurationSection("players");
        if (playersSection == null) {
            plugin.getLogger().warning("Секция players в player.yml отсутствует!");
            return members;
        }

        for (String player : playersSection.getKeys(false)) {
            String clan = playerConfig.getString("players." + player);
            if (clanName.equals(clan)) {
                members.add(player);
            }
        }

        plugin.getLogger().info("getClanMembers: клан " + clanName + ", участники: " + members);
        return members;
    }

    private List<String> getClanTerritory(String clanName) {
        if (territoryConfig == null) {
            plugin.getLogger().warning("territoryConfig не загружен!");
            return Collections.emptyList();
        }

        List<String> chunks = new ArrayList<>();

        List<?> rawList = territoryConfig.getList("territories." + clanName);
        if (rawList == null) {
            plugin.getLogger().info("getClanTerritory: территория клана " + clanName + " не найдена");
            return chunks;
        }

        for (Object item : rawList) {
            if (item instanceof List<?>) {
                List<?> coordList = (List<?>) item;
                String coord = coordList.stream().map(Object::toString).collect(Collectors.joining(","));
                chunks.add(coord);
            } else if (item instanceof String) {
                chunks.add((String) item);
            }
        }

        plugin.getLogger().info("getClanTerritory: клан " + clanName + ", чанки: " + chunks);
        return chunks;
    }

    // Вложенный класс для кеша
    private static class CacheEntry {
        final String value;
        final long timestamp;

        CacheEntry(String value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
