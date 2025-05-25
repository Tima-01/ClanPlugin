package org.plugin.clansPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class ClanManager {
    private final java.util.Map<String, Boolean> friendlyFireMap = new java.util.HashMap<>();

    private final JavaPlugin plugin;
    private List<String> clans;

    public ClanManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveResource("clans.yml", false);
    }

    /**
     * Перезагружает список кланов из файла clans.yml (внутри плагина).
     */
    public void reloadClans() {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(clansFile);
        this.clans = cfg.getStringList("clans");

        friendlyFireMap.clear();
        for (String clan : clans) {
            boolean friendlyFire = cfg.getBoolean("friendlyFire." + clan, false);
            friendlyFireMap.put(clan, friendlyFire);
        }
    }
    public boolean isFriendlyFire(String clanName) {
        return friendlyFireMap.getOrDefault(clanName, false);
    }

    public void setFriendlyFire(String clanName, boolean enabled) {
        friendlyFireMap.put(clanName, enabled);
        saveFriendlyFire(clanName, enabled);
    }
    private void saveFriendlyFire(String clanName, boolean enabled) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(clansFile);

        cfg.set("friendlyFire." + clanName, enabled);

        try {
            cfg.save(clansFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Возвращает список допустимых названий кланов.
     */
    public List<String> getClans() {
        return clans;
    }

    /**
     * Проверяет, существует ли клан с таким именем.
     */
    public boolean clanExists(String clanName) {
        return clans != null && clans.contains(clanName);
    }
}
