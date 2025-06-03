package org.plugin.clansPlugin.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class ClanManager {

    private final JavaPlugin plugin;
    private final File clansFile;
    private FileConfiguration clansConfig;

    private List<String> clans;

    public ClanManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.clansFile = new File(plugin.getDataFolder(), "clans.yml");

        if (!clansFile.exists()) {
            plugin.saveResource("clans.yml", false);
        }

        this.clansConfig = YamlConfiguration.loadConfiguration(clansFile);
        loadClans();
    }

    /**
     * Перезагружает список кланов из файла clans.yml.
     */
    public void reloadClans() {
        this.clansConfig = YamlConfiguration.loadConfiguration(clansFile);
        loadClans();
    }

    /**
     * Загружает кланы из конфигурации.
     */
    private void loadClans() {
        this.clans = clansConfig.getStringList("clans");
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
