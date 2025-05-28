package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SubCommandHelp implements SubCommand {

    private final Map<String, SubCommand> allCommands;

    public SubCommandHelp(Map<String, SubCommand> allCommands) {
        this.allCommands = allCommands;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"help"};
    }

    @Override
    public String getUsage() {
        return "/clan help";
    }

    @Override
    public String getDescription() {
        return "Показать список всех команд";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        player.sendMessage(ChatColor.GOLD + "=====[ Команды /clan ]=====");
        // Выводим описание уникальных SubCommand (не дублируем для разных алиасов)
        Set<SubCommand> unique = new HashSet<>(allCommands.values());
        for (SubCommand cmd : unique) {
            player.sendMessage(ChatColor.YELLOW + cmd.getUsage() + ChatColor.WHITE + " — " + cmd.getDescription());
        }
        player.sendMessage(ChatColor.GOLD + "=========================");
        return true;
    }
}
