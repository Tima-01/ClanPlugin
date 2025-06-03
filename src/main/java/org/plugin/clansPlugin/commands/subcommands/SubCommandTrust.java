package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class SubCommandTrust implements SubCommand {
    private final PlayerDataManager pdm;

    public SubCommandTrust(PlayerDataManager pdm) {
        this.pdm = pdm;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"trust"};
    }

    @Override
    public String getUsage() {
        return "/clan trust <игрок>";
    }

    @Override
    public String getDescription() {
        return "Выдать/забрать доверие игроку";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String playerName = player.getName();
        String targetName = args[0];

        // Проверка, что игрок — лидер клана
        if (!pdm.isClanLeader(playerName)) {
            player.sendMessage(ChatColor.RED + "Только лидер клана может выдавать доверие.");
            return true;
        }

        // Проверка, что цель — участник клана
        if (!pdm.getPlayerClan(targetName).equals(pdm.getPlayerClan(playerName))) {
            player.sendMessage(ChatColor.RED + "Игрок не состоит в вашем клане.");
            return true;
        }

        boolean isTrusted = pdm.hasTrust(targetName);
        pdm.setTrust(targetName, !isTrusted);

        player.sendMessage(ChatColor.GREEN + "Доверие игрока " + targetName +
                (isTrusted ? " §cотозвано" : " §aвыдано") + "§7.");
        return true;
    }
}