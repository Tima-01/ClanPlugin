package org.plugin.clansPlugin.commands;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class ChatClCommand implements CommandExecutor {

    private final ClansPlugin plugin;

    public ChatClCommand(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /chatcl <сообщение>");
            return true;
        }

        PlayerDataManager pdm = plugin.getPlayerDataManager();
        String clan = pdm.getPlayerClan(player.getName());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
            return true;
        }

        String message = String.join(" ", args);
        String formatted = ChatColor.DARK_AQUA + "[Клан: " + clan + "] "
                + ChatColor.GRAY + player.getName() + ": "
                + ChatColor.WHITE + message;

        // Рассылаем сообщение только тем, у кого включён clanChatToggle
        for (Player p : Bukkit.getOnlinePlayers()) {
            String pName = p.getName();
            boolean sameClan = clan.equals(pdm.getPlayerClan(pName));
            boolean isAdmin = p.hasPermission("clan.admin");

            if ((sameClan && pdm.isClanChatToggled(pName)) || isAdmin) {
                p.sendMessage(formatted);
            }
        }

        return true;
    }
}

