package org.plugin.clansPlugin.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.plugin.clansPlugin.ClansPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            case "base": {
                if (clanParam == null) return "Не указан клан";
                Location base = plugin.getTerritoryManager().getClanBaseCenter(clanParam);
                return base != null
                        ? String.format("X: %d, Y: %d, Z: %d", base.getBlockX(), base.getBlockY(), base.getBlockZ())
                        : "Пока нет";
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

        List<String> chunks = new ArrayList<>();
        List<?> rawList = territoryConfig.getList("territories." + clanName);
        if (rawList == null) return chunks;

        for (Object item : rawList) {
            if (item instanceof List<?>) {
                List<?> coords = (List<?>) item;
                String coord = String.join(",", coords.stream().map(Object::toString).toArray(String[]::new));
                chunks.add(coord);
            } else if (item instanceof String) {
                chunks.add((String) item);
            }
        }

        return chunks;
    }
}
