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
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class FlagListener implements Listener {
    private final TerritoryManager territoryManager;
    private final ClansPlugin plugin;

    public FlagListener(TerritoryManager territoryManager, ClansPlugin plugin) {
        this.territoryManager = territoryManager;
        this.plugin = plugin;
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
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            if (armorStand.getCustomName() != null && armorStand.getCustomName().contains("Флаг")) {
                // Проверяем, является ли атакующий игроком
                if (event.getDamager() instanceof Player) {
                    Player player = (Player) event.getDamager();
                    // Получаем местоположение ArmorStand (которое совпадает с флагом)
                    Location flagLocation = armorStand.getLocation().getBlock().getLocation();
                    // Вызываем метод damageFlag
                    plugin.getTerritoryManager().damageFlag(flagLocation, player, 5); // 5 - пример урона
                    event.setCancelled(true); // Отменяем стандартный урон
                }
            }
        }
    }
}