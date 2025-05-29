package org.plugin.clansPlugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.buffs.ClanBuff;
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
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || from.getChunk().equals(to.getChunk())) {
            return; // игрок остался в том же чанке
        }

        Player player = event.getPlayer();
        int chunkX = to.getChunk().getX();
        int chunkZ = to.getChunk().getZ();

        TerritoryManager territoryManager = plugin.getTerritoryManager();
        String clanHere = territoryManager.getClanByChunk(chunkX, chunkZ);
        String lastClan = playerCurrentClan.get(player.getName());

        if (clanHere != null && !clanHere.equals(lastClan)) {
            player.sendTitle(
                    ChatColor.GREEN + "Вы вошли на территорию клана",
                    ChatColor.AQUA + clanHere,
                    10, 70, 20
            );
            playerCurrentClan.put(player.getName(), clanHere);
        } else if (clanHere == null && lastClan != null) {
            player.sendTitle(
                    ChatColor.RED + "Вы покинули территорию клана",
                    ChatColor.AQUA + lastClan,
                    10, 70, 20
            );
            playerCurrentClan.remove(player.getName());
        }

        String playerClan = plugin.getPlayerDataManager().getPlayerClan(player.getName());
        if (playerClan == null) return;

        ClanBuff buff = plugin.getClanBuffManager().getBuff(playerClan);
        if (buff == null) return;

        boolean onOwnTerritory = clanHere != null && clanHere.equalsIgnoreCase(playerClan);
        // Основной эффект
        toggleEffect(player, buff.getPrimaryEffect(), buff.getPrimaryAmplifier(), onOwnTerritory);
        // Вторичный эффект
        if (buff.getSecondaryEffect() != null) {
            toggleEffect(player, buff.getSecondaryEffect(), buff.getSecondaryAmplifier(), onOwnTerritory);
        }
    }

    private void toggleEffect(Player player, PotionEffectType type, int amplifier, boolean apply) {
        if (apply) {
            player.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, amplifier, false, false));
        } else {
            player.removePotionEffect(type);
        }
    }
}
