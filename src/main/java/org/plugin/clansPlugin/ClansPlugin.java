package org.plugin.clansPlugin;


import org.bukkit.plugin.java.JavaPlugin;
import org.plugin.clansPlugin.managers.ClanManager;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;
import org.plugin.clansPlugin.commands.ChatClCommand;
import org.plugin.clansPlugin.commands.ClanChatCommand;
import org.plugin.clansPlugin.commands.ClanCommand;
import org.plugin.clansPlugin.listeners.InventoryClickListener;
import org.plugin.clansPlugin.listeners.PlayerChatListener;
import org.plugin.clansPlugin.listeners.PlayerJoinListener;
import org.plugin.clansPlugin.listeners.PlayerMoveListener;
import org.plugin.clansPlugin.placeholders.ClanExpansion;

public class ClansPlugin extends JavaPlugin {

    private ClanManager clanManager;
    private PlayerDataManager playerDataManager;
    private TerritoryManager territoryManager;

    @Override
    public void onEnable() {
        // 1) Инициализируем менеджеры
        clanManager = new ClanManager(this);
        playerDataManager = new PlayerDataManager(this);
        territoryManager = new TerritoryManager(this);

        // 2) Загрузка конфигураций
        clanManager.reloadClans();           // загружает clans.yml
        playerDataManager.initPlayerFile();  // инициализация players.yml
        territoryManager.initTerritoryFile();// инициализация territories.yml

        // 3) Регистрируем слушателей
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);

        // 4) Регистрируем команды
        getCommand("chatcl").setExecutor(new ChatClCommand(this));
        getCommand("clanchat").setExecutor(new ClanChatCommand(this));
        getCommand("clan").setExecutor(new ClanCommand(this));

        getLogger().info("ClansPlugin включен.");

        // 5) Регистрация плейсхолдеров
        new ClanExpansion(this).register();
    }

    @Override
    public void onDisable() {
        // При выключении сохраняем файлы
        playerDataManager.savePlayerData();
        territoryManager.saveTerritoryData();
        getLogger().info("ClansPlugin отключен.");
    }

    // Геттеры для доступа к менеджерам из других классов
    public ClanManager getClanManager() {
        return clanManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public TerritoryManager getTerritoryManager() {
        return territoryManager;
    }
}
