package org.plugin.clansPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class ClanManager {

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
