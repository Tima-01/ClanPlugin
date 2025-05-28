package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;

public class SubCommandReload implements SubCommand {

    private final ClansPlugin plugin;

    public SubCommandReload(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"reload"};
    }

    @Override
    public String getUsage() {
        return "/clan reload";
    }

    @Override
    public String getDescription() {
        return "Перезагрузить конфиги плагина (только админ)";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!player.hasPermission("clan.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для этой команды.");
            return true;
        }
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        plugin.getClanManager().reloadClans();
        plugin.getPlayerDataManager().initPlayerFile();
        plugin.getTerritoryManager().initTerritoryFile();
        player.sendMessage(ChatColor.GREEN + "ClansPlugin: Все конфигурации были перезагружены.");
        return true;
    }
}
