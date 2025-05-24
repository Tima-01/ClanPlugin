package org.plugin.clansPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.plugin.clansPlugin.commands.*;
import org.plugin.clansPlugin.managers.ClanManager;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;
import org.plugin.clansPlugin.managers.VoteManager;
import org.plugin.clansPlugin.listeners.InventoryClickListener;
import org.plugin.clansPlugin.listeners.PlayerChatListener;
import org.plugin.clansPlugin.listeners.PlayerJoinListener;
import org.plugin.clansPlugin.listeners.PlayerMoveListener;
import org.plugin.clansPlugin.placeholders.ClanExpansion;
import org.plugin.clansPlugin.commands.StartVoteCommand;

public class ClansPlugin extends JavaPlugin {

    private ClanManager clanManager;
    private PlayerDataManager playerDataManager;
    private TerritoryManager territoryManager;
    private VoteManager voteManager;

    @Override
    public void onEnable() {
        // 1) Инициализация менеджеров (создаем один раз и сохраняем в поле)
        clanManager = new ClanManager(this);
        playerDataManager = new PlayerDataManager(this);
        territoryManager = new TerritoryManager(this);
        voteManager = new VoteManager(this, playerDataManager);

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

        getCommand("endvote").setExecutor(new EndVoteCommand(voteManager));
        getCommand("showbase").setExecutor(new ShowBaseCommand(territoryManager, playerDataManager));
        getCommand("chatcl").setExecutor(new ChatClCommand(this));
        getCommand("clanchat").setExecutor(new ClanChatCommand(this));
        getCommand("clan").setExecutor(new ClanCommand(this));
        getCommand("startvote").setExecutor(new StartVoteCommand(voteManager));

        getCommand("addplayer").setExecutor(new AddPlayerCommand(playerDataManager));
        getCommand("removeplayer").setExecutor(new RemovePlayerCommand(playerDataManager));

        // Если нужна команда для удаления лидера, лучше дать ей отдельное имя, например /removeleader
        // getCommand("removeleader").setExecutor(new RemoveLeaderCommand(playerDataManager));

        getCommand("votel").setExecutor(new VoteCommand(voteManager));

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
    public VoteManager getVoteManager() {
        return voteManager;
    }
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    public TerritoryManager getTerritoryManager() {
        return territoryManager;
    }
}
