package org.plugin.clansPlugin.commands;



import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class ClanChatCommand implements CommandExecutor {

    private final ClansPlugin plugin;

    public ClanChatCommand(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            PlayerDataManager pdm = plugin.getPlayerDataManager();
            String name = player.getName();

            if (pdm.isPlayerInClan(name) == false) {
                player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                return true;
            }

            if (pdm.isClanChatSendMode(name)) {
                pdm.setClanChatSendMode(name, false);
                player.sendMessage(ChatColor.GRAY + "Режим клан-чата выключен. Сообщения отправляются в обычный чат.");
            } else {
                pdm.setClanChatSendMode(name, true);
                player.sendMessage(ChatColor.GREEN + "Режим клан-чата включён. Все сообщения будут отправляться в клан.");
            }
            pdm.savePlayerData(); // сохраняем изменения в players.yml, если список sendMode хранится там
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Использование: /clanchat toggle");
            return true;
        }
    }
}

