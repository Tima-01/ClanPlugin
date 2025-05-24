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
import java.util.*;
import java.util.stream.Collectors;

public class ClanExpansion extends PlaceholderExpansion {

    private final ClansPlugin plugin;
    private final File dataFolder;

    private FileConfiguration playerConfig;
    private FileConfiguration territoryConfig;

    // Кеш: ключ — игрок+идентификатор, значение — CacheEntry с результатом и временем обновления
    private final Map<String, CacheEntry> cache = new HashMap<>();

    // Время кеширования в миллисекундах (60 минут)
    private static final long CACHE_TIME = 1 * 60 * 1000;

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

        String[] parts = identifier.split(":", 2); // <= ограничиваем split до 2 частей!
        String baseIdentifier = parts[0].toLowerCase();
        String targetClan = parts.length > 1 ? parts[1] : null;

        // Получаем имя клана: если указано явно — используем его, иначе берём у игрока
        String clanName;
        if (targetClan != null) {
            clanName = targetClan;
        } else {
            clanName = getPlayerClan(player.getName());
            plugin.getLogger().info("[ClansPlugin] getPlayerClan: игрок " + player.getName() + ", клан: " + clanName);
        }

        plugin.getLogger().info("[ClansPlugin] Клан " + (targetClan != null ? "(указан явно): " : "игрока ") + clanName);

        if (clanName == null || clanName.isEmpty()) {
            plugin.getLogger().info("[ClansPlugin] Клан не найден" + (targetClan != null ? " (не существует?)" : " у игрока " + player.getName()));
            return "";
        }

        String key = (targetClan != null ? "@" + clanName.toLowerCase() : player.getName().toLowerCase()) + ":" + baseIdentifier;
        long now = System.currentTimeMillis();

        CacheEntry cached = cache.get(key);
        if (cached != null && (now - cached.timestamp) < CACHE_TIME) {
            return cached.value;
        }

        String result;
        switch (baseIdentifier) {
            case "tag":
                result = " [" + clanName + "]";
                break;
            case "leader":
                result = Optional.ofNullable(getClanLeader(clanName)).orElse("Нет лидера");
                break;
            case "members":
                result = Optional.ofNullable(getClanMembers(clanName)).map(m -> String.valueOf(m.size())).orElse("0");
                break;
            case "territory":
                result = Optional.ofNullable(getClanTerritory(clanName)).map(t -> String.valueOf(t.size())).orElse("0");
                break;
            case "base":
                Location base = plugin.getTerritoryManager().getClanBaseCenter(clanName);
                result = base != null
                        ? String.format("X: %d, Y: %d, Z: %d", base.getBlockX(), base.getBlockY(), base.getBlockZ())
                        : "Пока нет";
                break;
            default:
                result = null;
        }
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
