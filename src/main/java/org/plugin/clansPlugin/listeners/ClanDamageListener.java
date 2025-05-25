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

        String clan1 = plugin.getPlayerDataManager().getPlayerClan(damager.getName());
        String clan2 = plugin.getPlayerDataManager().getPlayerClan(target.getName());

        if (clan1 != null && clan1.equals(clan2)) {
            boolean pvpAllowed = plugin.getPlayerDataManager().isClanPvpEnabled(clan1);
            if (!pvpAllowed) {
                event.setCancelled(true);
                damager.sendMessage("PVP между соклановцами отключено.");
            }
        }
    }
}
