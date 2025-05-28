package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class SubCommandChatToggle implements SubCommand {

    private final PlayerDataManager pdm;

    public SubCommandChatToggle(PlayerDataManager pdm) {
        this.pdm = pdm;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"chat"};
    }

    @Override
    public String getUsage() {
        return "/clan chat";
    }

    @Override
    public String getDescription() {
        return "Включить/выключить видимость клан-чата";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String playerName = player.getName();
        if (!pdm.isPlayerInClan(playerName)) {
            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
            return true;
        }

        boolean currently = pdm.isClanChatToggled(playerName);
        pdm.setClanChatToggled(playerName, !currently);

        if (currently) {
            player.sendMessage(ChatColor.GRAY + "Клан-чат выключен.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Клан-чат включён.");
        }
        return true;
    }
}
