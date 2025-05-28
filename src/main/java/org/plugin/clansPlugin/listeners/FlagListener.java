package org.plugin.clansPlugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Banner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class FlagListener implements Listener {
    private final TerritoryManager territoryManager;

    public FlagListener(TerritoryManager territoryManager) {
        this.territoryManager = territoryManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Защита баннера флага
        if (event.getBlock().getState() instanceof Banner) {
            String owner = territoryManager.getFlagOwner(event.getBlock().getLocation());
            if (owner != null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Флаги нельзя разрушать напрямую! Атакуйте их, чтобы захватить территорию.");
            }
        }
    }

    @EventHandler
    public void onPlayerDamageFlag(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();

        // Проверяем, что игрок атакует индикатор здоровья флага
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            if (armorStand.getCustomName() != null && armorStand.getCustomName().contains("Флаг")) {
                event.setCancelled(true);

                // Получаем местоположение флага (под ArmorStand)
                Location flagLocation = armorStand.getLocation().clone()
                        .subtract(0.5, 0, 0.5)
                        .getBlock()
                        .getLocation();

                // Наносим урон флагу
                territoryManager.damageFlag(flagLocation, player, 10);
            }
        }
    }
}