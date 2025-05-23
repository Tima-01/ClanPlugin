package org.plugin.clansPlugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.HashMap;
import java.util.Map;

public class PlayerMoveListener implements Listener {

    private final ClansPlugin plugin;
    // Здесь храним последнее «находящийся на территории клана» для каждого игрока
    private final Map<String, String> playerCurrentClan = new HashMap<>();

    public PlayerMoveListener(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        int chunkX = loc.getChunk().getX();
        int chunkZ = loc.getChunk().getZ();

        TerritoryManager tm = plugin.getTerritoryManager();

        String clanHere = tm.getClanByChunk(chunkX, chunkZ);
        String lastClan = playerCurrentClan.get(player.getName());

        if (clanHere != null && !clanHere.equals(lastClan)) {
            // Игрок вошёл на территорию клана
            player.sendTitle(
                    ChatColor.GREEN + "Вы вошли на территорию клана",
                    ChatColor.AQUA + clanHere,
                    10, 70, 20
            );
            playerCurrentClan.put(player.getName(), clanHere);
        } else if (clanHere == null && lastClan != null) {
            // Игрок покинул территорию клана
            player.sendTitle(
                    ChatColor.RED + "Вы покинули территорию клана",
                    ChatColor.AQUA + lastClan,
                    10, 70, 20
            );
            playerCurrentClan.remove(player.getName());
        }
    }
}
