package org.plugin.clansPlugin.listeners;


import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.PlayerDataManager;

public class PlayerChatListener implements Listener {

    private final ClansPlugin plugin;

    public PlayerChatListener(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager pdm = plugin.getPlayerDataManager();

        // Если игрок в режиме "отправлять всё в клан-чат"
        if (pdm.isClanChatSendMode(player.getName())) {
            String clan = pdm.getPlayerClan(player.getName());
            if (clan == null) {
                player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                return;
            }

            String message = event.getMessage();
            String formatted = ChatColor.DARK_AQUA + "[Клан: " + clan + "] "
                    + ChatColor.GRAY + player.getName() + ": "
                    + ChatColor.WHITE + message;

            event.setCancelled(true); // отменяем стандартный чат

            for (Player p : Bukkit.getOnlinePlayers()) {
                String pClan = pdm.getPlayerClan(p.getName());
                if (clan.equals(pClan)) {
                    p.sendMessage(formatted);
                }
            }
        }
    }
}
