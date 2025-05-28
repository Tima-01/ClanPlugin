package org.plugin.clansPlugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;

public class ClanDamageListener implements Listener {

    private final ClansPlugin plugin;

    public ClanDamageListener(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClanDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player target)) return;

        String damagerClan = plugin.getPlayerDataManager().getPlayerClan(damager.getName());
        String targetClan = plugin.getPlayerDataManager().getPlayerClan(target.getName());

        // Только если оба игрока в одном клане и pvp запрещено
        if (damagerClan != null && damagerClan.equals(targetClan)) {
            boolean isPvpEnabled = plugin.getPlayerDataManager().isClanPvpEnabled(damagerClan);
            if (!isPvpEnabled) {
                event.setCancelled(true);
                damager.sendMessage("PVP между соклановцами отключено.");
            }
        }
    }

}
