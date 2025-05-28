package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.gui.BuffSelectionGUI;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class SubCommandSetBuff implements SubCommand {

    private final PlayerDataManager pdm;
    private final ClansPlugin plugin;

    public SubCommandSetBuff(PlayerDataManager pdm, ClansPlugin plugin) {
        this.pdm = pdm;
        this.plugin = plugin;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"setbuff"};
    }

    @Override
    public String getUsage() {
        return "/clan setbuff";
    }

    @Override
    public String getDescription() {
        return "Открыть меню выбора баффа (только лидер)";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String playerName = player.getName();
        String clanName = pdm.getPlayerClan(playerName);
        if (clanName == null) {
            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
            return true;
        }

        String leader = pdm.getClanLeader(clanName);
        if (!playerName.equalsIgnoreCase(leader)) {
            player.sendMessage(ChatColor.RED + "Только лидер клана может назначать баффы.");
            return true;
        }

        // Открываем GUI выбора баффа
        BuffSelectionGUI gui = new BuffSelectionGUI(plugin.getClanBuffManager(), clanName, player);
        gui.open();
        return true;
    }
}
