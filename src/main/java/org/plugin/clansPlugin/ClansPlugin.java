package org.plugin.clansPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.plugin.clansPlugin.commands.*;
import org.plugin.clansPlugin.managers.*;
import org.plugin.clansPlugin.listeners.InventoryClickListener;
import org.plugin.clansPlugin.listeners.PlayerChatListener;
import org.plugin.clansPlugin.listeners.PlayerJoinListener;
import org.plugin.clansPlugin.listeners.PlayerMoveListener;
import org.plugin.clansPlugin.placeholders.ClanExpansion;

public class ClansPlugin extends JavaPlugin {

    private static ClansPlugin instance;

    private ClanManager clanManager;
    private PlayerDataManager playerDataManager;
    private TerritoryManager territoryManager;
    private VoteManager voteManager;
    private ClanExpansion clanExpansion;
    private ClanBuffManager clanBuffManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1) Инициализация менеджеров
        clanManager = new ClanManager(this);
        playerDataManager = new PlayerDataManager(this);
        territoryManager = new TerritoryManager(this);
        voteManager = new VoteManager(this, playerDataManager);
        long cooldownMinutes = getConfig().getLong("buff.cooldown_minutes", 30);
        clanBuffManager = new ClanBuffManager(this,cooldownMinutes * 60 * 1000);

        // 2) Загрузка конфигураций
        clanManager.reloadClans();
        playerDataManager.initPlayerFile();
        territoryManager.initTerritoryFile();

        // 3) Регистрируем слушателей
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);

        // 4) Регистрируем команды
        getCommand("clanadmin").setExecutor(new ClanAdminCommand(playerDataManager, territoryManager, clanManager));
        getCommand("endvote").setExecutor(new EndVoteCommand(voteManager));
        getCommand("showbase").setExecutor(new ShowBaseCommand(territoryManager, playerDataManager)); // Можно использовать getInstance() внутри
        getCommand("chatcl").setExecutor(new ChatClCommand(this));
        getCommand("clanchat").setExecutor(new ClanChatCommand(this));
        getCommand("clan").setExecutor(new ClanCommand(this));
        getCommand("startvote").setExecutor(new StartVoteCommand(voteManager));
        getCommand("addplayer").setExecutor(new AddPlayerCommand(playerDataManager));
        getCommand("removeplayer").setExecutor(new RemovePlayerCommand(playerDataManager));
        getCommand("votel").setExecutor(new VoteCommand(voteManager));

        getLogger().info("ClansPlugin включен.");


        // 5) Регистрация плейсхолдеров
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            clanExpansion = new ClanExpansion(this);
            clanExpansion.register();
        }

        // 5) Плейсхолдеры
        new ClanExpansion(this).register();

    }

    @Override
    public void onDisable() {
        playerDataManager.savePlayerData();
        territoryManager.saveTerritoryData();
        getLogger().info("ClansPlugin отключен.");
    }

    // === Геттеры ===
    public static ClansPlugin getInstance() {
        return instance;
    }

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
    public ClanBuffManager getClanBuffManager() { return clanBuffManager; }
}
