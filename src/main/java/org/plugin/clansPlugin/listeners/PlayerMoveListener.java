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

import java.util.HashMap;
import java.util.Map;

public class PlayerMoveListener implements Listener {

    private final ClansPlugin plugin;
    private final Map<String, String> playerCurrentClan = new HashMap<>();

    public PlayerMoveListener(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    private void toggleEffect(Player player, PotionEffectType type, int amplifier, boolean apply) {
        if (apply) {
            player.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, amplifier, false, false));
        } else {
            player.removePotionEffect(type);
        }
    }

    public void updatePlayerTerritoryStatus(Player player, String currentClan) {
        String lastClan = this.playerCurrentClan.get(player.getName());
        String playerClan = plugin.getPlayerDataManager().getPlayerClan(player.getName());

        // Check territory status change
        if (currentClan != null && !currentClan.equals(lastClan)) {
            player.sendTitle(
                    ChatColor.GREEN + "Вы вошли на территорию клана",
                    ChatColor.AQUA + currentClan,
                    10, 70, 20
            );
            this.playerCurrentClan.put(player.getName(), currentClan);
        } else if (currentClan == null && lastClan != null) {
            player.sendTitle(
                    ChatColor.RED + "Вы покинули территорию клана",
                    ChatColor.AQUA + lastClan,
                    10, 70, 20
            );
            this.playerCurrentClan.remove(player.getName());
        }

        // Update buffs
        this.updatePlayerBuffs(player, playerClan, currentClan);
    }

    private void updatePlayerBuffs(Player player, String playerClan, String currentClan) {
        if (playerClan == null) return;

        ClanBuff buff = plugin.getClanBuffManager().getBuff(playerClan);
        if (buff == null) return;

        boolean onOwnTerritory = currentClan != null && currentClan.equalsIgnoreCase(playerClan);
        this.toggleEffect(player, buff.getPrimaryEffect(), buff.getPrimaryAmplifier(), onOwnTerritory);
        if (buff.getSecondaryEffect() != null) {
            this.toggleEffect(player, buff.getSecondaryEffect(), buff.getSecondaryAmplifier(), onOwnTerritory);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only process if player actually moved to a new block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }

        Player player = event.getPlayer();
        Location loc = player.getLocation();
        int chunkX = loc.getChunk().getX();
        int chunkZ = loc.getChunk().getZ();

        String clanHere = plugin.getTerritoryManager().getClanByChunk(chunkX, chunkZ);
        this.updatePlayerTerritoryStatus(player, clanHere);
    }
}