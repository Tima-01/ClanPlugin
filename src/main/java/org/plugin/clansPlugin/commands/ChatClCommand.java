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

        // Рассылаем сообщение только тем, у кого включён clanChatToggle (т. е. находится в clanChatToggles)
        // Но у нас списка toggle нет в pdm — этот список можно перенести в pdm, либо хранить локально (например, static).
        // Предположим, что pdm хранит также List<String> clanChatToggles:
        for (Player p : Bukkit.getOnlinePlayers()) {
            String pClan = pdm.getPlayerClan(p.getName());
            if (clan.equals(pClan) && pdm.isClanChatToggled(p.getName())) {
                p.sendMessage(formatted);
            }
        }

        return true;
    }
}

