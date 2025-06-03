package org.plugin.clansPlugin.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.plugin.clansPlugin.ClansPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ClanExpansion extends PlaceholderExpansion {

    private final ClansPlugin plugin;
    private FileConfiguration playerConfig;
    private FileConfiguration territoryConfig;

    private File playerFile;
    private File territoryFile;

    public ClanExpansion(ClansPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        try {
            playerFile = new File(plugin.getDataFolder(), "players.yml");
            territoryFile = new File(plugin.getDataFolder(), "territories.yml");

            if (playerFile.exists()) {
                playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            }

            if (territoryFile.exists()) {
                territoryConfig = YamlConfiguration.loadConfiguration(territoryFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Чтобы обновлять конфиги, например при изменениях
    public void reloadConfigs() {
        if (playerFile != null && playerFile.exists()) {
            playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        }
        if (territoryFile != null && territoryFile.exists()) {
            territoryConfig = YamlConfiguration.loadConfiguration(territoryFile);
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

        // Перезагружаем конфиги каждый раз
        reloadConfigs();

        // Обработка параметров в плейсхолдерах
        String[] parts = identifier.split(":", 2);
        String key = parts[0].toLowerCase();
        String clanParam = parts.length > 1 ? parts[1] : null;

        switch (key) {
            case "tag": {
                String clanName = plugin.getPlayerDataManager().getPlayerClan(player.getName());
                return clanName != null ? " [" + clanName + "]" : "";
            }
            case "leader": {
                if (clanParam == null) return "Не указан клан";
                return getClanLeader(clanParam);
            }
            case "members": {
                if (clanParam == null) return "Не указан клан";
                return String.valueOf(getClanMembers(clanParam).size());
            }
            case "territory": {
                if (clanParam == null) return "Не указан клан";
                return String.valueOf(getClanTerritory(clanParam).size());
            }
            case "name": {
                String clanName = plugin.getPlayerDataManager().getPlayerClan(player.getName());
                return clanName != null ? clanName : "";
            }
            case "base": {
                if (clanParam == null) return "Не указан клан";

                String playerClan = plugin.getPlayerDataManager().getPlayerClan(player.getName());
                if (playerClan == null || !playerClan.equalsIgnoreCase(clanParam)) {
                    return "§cВы из другого клана!";
                }

                // Проверка доверия
                if (!plugin.getPlayerDataManager().isClanLeader(player.getName()) &&
                        !plugin.getPlayerDataManager().hasTrust(player.getName())) {
                    return "§cЛидер вам не доверяет";
                }

                Location base = plugin.getTerritoryManager().getClanBaseCenter(clanParam);
                return base != null
                        ? String.format("§aX: %d, Y: %d, Z: %d", base.getBlockX(), base.getBlockY(), base.getBlockZ())
                        : "§eБаза не установлена";
            }

            default:
                return null;
        }
    }

    private String getClanLeader(String clanName) {
        if (playerConfig == null) return "Данные не загружены";
        return playerConfig.getString("leaders." + clanName, "Данные не загружены");
    }

    private List<String> getClanMembers(String clanName) {
        if (playerConfig == null) return Collections.emptyList();

        List<String> members = new ArrayList<>();
        for (String player : playerConfig.getConfigurationSection("players").getKeys(false)) {
            String clan = playerConfig.getString("players." + player);
            if (clanName.equalsIgnoreCase(clan)) {
                members.add(player);
            }
        }
        return members;
    }

    private List<String> getClanTerritory(String clanName) {
        if (territoryConfig == null) return Collections.emptyList();

        Set<String> uniqueChunks = new HashSet<>();

        // 1. Обрабатываем основную территорию
        String baseCoords = territoryConfig.getString("territories." + clanName);
        if (baseCoords != null) {
            addChunksFromCoords(uniqueChunks, baseCoords);
        }

        // 2. Обрабатываем территории флагов
        if (territoryConfig.contains("flags." + clanName)) {
            for (String flagId : territoryConfig.getConfigurationSection("flags." + clanName).getKeys(false)) {
                String flagData = territoryConfig.getString("flags." + clanName + "." + flagId);
                if (flagData != null) {
                    String[] parts = flagData.split(",");
                    if (parts.length >= 4) {
                        addChunksFromCoords(uniqueChunks, parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3]);
                    }
                }
            }
        }

        return new ArrayList<>(uniqueChunks);
    }

    private void addChunksFromCoords(Set<String> chunks, String coords) {
        String[] parts = coords.split(",");
        if (parts.length == 4) {
            try {
                int x1 = Integer.parseInt(parts[0]);
                int z1 = Integer.parseInt(parts[1]);
                int x2 = Integer.parseInt(parts[2]);
                int z2 = Integer.parseInt(parts[3]);

                int minX = Math.min(x1, x2);
                int maxX = Math.max(x1, x2);
                int minZ = Math.min(z1, z2);
                int maxZ = Math.max(z1, z2);

                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        chunks.add(x + "," + z);
                    }
                }
            } catch (NumberFormatException e) {
                // Логирование ошибки при необходимости
            }
        }
    }
}
