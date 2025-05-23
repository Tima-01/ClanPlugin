package org.plugin.clansPlugin.managers;


import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerDataManager {

    private final JavaPlugin plugin;
    private File playerFile;
    private YamlConfiguration playerData;

    // ----------------------------------------
    // Поле для хранения включённого/выключенного ClanChat-видимого состояния:
    private List<String> clanChatToggles = new ArrayList<>();

    // Поле для хранения режима "всё в клан-чат":
    private List<String> clanChatSendMode = new ArrayList<>();
    // ----------------------------------------

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Если у вас есть дефолтный players.yml в ресурсах, можно сразу сохранить его:
        // plugin.saveResource("players.yml", false);
    }

    public void initPlayerFile() {
        playerFile = new File(plugin.getDataFolder(), "players.yml");
        if (!playerFile.exists()) {
            try {
                playerFile.getParentFile().mkdirs();
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(playerFile);

        // Если вы хотите, чтобы состояния переключателей (toggles и sendMode) сохранялись между
        // перезагрузками, здесь нужно подгрузить их из players.yml:
        if (playerData.contains("chatToggles")) {
            clanChatToggles = playerData.getStringList("chatToggles");
        }
        if (playerData.contains("chatSendMode")) {
            clanChatSendMode = playerData.getStringList("chatSendMode");
        }
    }

    public void savePlayerData() {
        // Перед сохранением записываем актуальные списки в конфигурацию:
        playerData.set("chatToggles", clanChatToggles);
        playerData.set("chatSendMode", clanChatSendMode);

        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================
    // Методы для работы с clanChatToggles
    // ============================

    /**
     * Проверяет, включён ли у игрока режим “видимость клан-чата”
     */
    public boolean isClanChatToggled(String playerName) {
        return clanChatToggles.contains(playerName);
    }

    /**
     * Устанавливает или сбрасывает режим “видимость клан-чата” для игрока
     */
    public void setClanChatToggled(String playerName, boolean toggled) {
        if (toggled) {
            if (!clanChatToggles.contains(playerName)) {
                clanChatToggles.add(playerName);
            }
        } else {
            clanChatToggles.remove(playerName);
        }
    }

    // ============================
    // Методы для работы с clanChatSendMode
    // ============================

    /**
     * Проверяет, включён ли у игрока режим “всё в клан-чат”
     */
    public boolean isClanChatSendMode(String playerName) {
        return clanChatSendMode.contains(playerName);
    }

    /**
     * Включает или выключает режим “всё в клан-чат” у игрока
     */
    public void setClanChatSendMode(String playerName, boolean sendMode) {
        if (sendMode) {
            if (!clanChatSendMode.contains(playerName)) {
                clanChatSendMode.add(playerName);
            }
        } else {
            clanChatSendMode.remove(playerName);
        }
    }

    // ============================
    // Остальные методы, связанные с игроками и кланами
    // ============================

    /**
     * Возвращает имя клана, в котором состоит игрок, или null, если игрок не в клане
     */
    public String getPlayerClan(String playerName) {
        return playerData.getString("players." + playerName);
    }


    /**
     * Устанавливает (или обновляет) клан для игрока
     */
    public void setPlayerClan(String playerName, String clanName) {
        playerData.set("players." + playerName, clanName);
        save();
    }
    public void save() {
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Удаляет игрока из клана (игрок выходит)
     */
    public void removePlayerFromClan(String playerName) {
        playerData.set("players." + playerName, null);
        save();
    }

    /**
     * Проверяет, состоит ли игрок в каком-либо клане
     */
    public boolean isPlayerInClan(String playerName) {
        return getPlayerClan(playerName) != null;
    }

    /**
     * Назначает лидера клана (записывает в секцию leaders.<clanName> = playerName)
     */
    public void setClanLeader(String clanName, String playerName) {
        playerData.set("leaders." + clanName, playerName);
    }

    /**
     * Возвращает имя лидера указанного клана или null, если не задано
     */
    public String getClanLeader(String clanName) {
        return playerData.getString("leaders." + clanName);
    }

    /**
     * Возвращает список всех игроков, принадлежащих указанному клану
     */
    public List<String> getClanMembers(String clanName) {
        ConfigurationSection section = playerData.getConfigurationSection("players");
        if (section == null) return List.of();
        List<String> members = new ArrayList<>();
        for (String name : section.getKeys(false)) {
            if (clanName.equals(playerData.getString("players." + name))) {
                members.add(name);
            }
        }
        return members;
    }
}

