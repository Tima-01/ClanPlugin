package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SubCommandHelp implements SubCommand {
    public SubCommandHelp() {

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

        player.sendMessage(ChatColor.GOLD + "=====[ Команды клана ]=====");
        player.sendMessage(ChatColor.YELLOW + "/clan info" + ChatColor.WHITE + " - Информация о твоем клане");
        player.sendMessage(ChatColor.YELLOW + "/clan tpbase" + ChatColor.WHITE + " - Телепорт на базу клана");
        player.sendMessage(ChatColor.YELLOW + "/clan leave" + ChatColor.WHITE + " - Покинуть клан");
        player.sendMessage(ChatColor.YELLOW + "/clan help" + ChatColor.WHITE + " - Показать это сообщение");
        player.sendMessage(ChatColor.YELLOW + "/clan join" + ChatColor.WHITE + " - Выбрать и вступить в клан");
        player.sendMessage(ChatColor.YELLOW + "/votel <игрок>" + ChatColor.WHITE + " - Проголосовать за участника клана");
        player.sendMessage(ChatColor.YELLOW + "/clan territories" + ChatColor.WHITE + " - Показать всю территорию клана");

        player.sendMessage(ChatColor.GOLD + "=====[ Чат клана ]=====");
        player.sendMessage(ChatColor.YELLOW + "/clanchat toggle" + ChatColor.WHITE + " - Включить/выключить режим отправки сообщений в клан-чат");
        player.sendMessage(ChatColor.YELLOW + "/clan chat" + ChatColor.WHITE + " - Включить/выключить видимость клан-чата");
        player.sendMessage(ChatColor.YELLOW + "/chatcl <сообщение>" + ChatColor.WHITE + " - Отправить сообщение в клан-чат");

        player.sendMessage(ChatColor.GOLD + "=====[ Команды лидера ]=====");
        player.sendMessage(ChatColor.YELLOW + "/clan createbase" + ChatColor.WHITE + " - Создать клановую базу");
        player.sendMessage(ChatColor.YELLOW + "/clan deletebase" + ChatColor.WHITE + " - Удалить базу клана");
        player.sendMessage(ChatColor.YELLOW + "/clan setbuff" + ChatColor.WHITE + " - Выбор баффа клана");
        player.sendMessage(ChatColor.YELLOW + "/clan createflag" + ChatColor.WHITE + " - Установить флаг для расширения территории");
        player.sendMessage(ChatColor.YELLOW + "/clan removeflag" + ChatColor.WHITE + " - Удалить флаг");
        player.sendMessage(ChatColor.YELLOW + "/removeplayer <игрок>" + ChatColor.WHITE + " - Удалить участника из клана");


        player.sendMessage(ChatColor.GOLD + "============================");

        return true;
    }
}
