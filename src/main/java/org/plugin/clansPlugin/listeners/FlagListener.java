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
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class FlagListener implements Listener {
    private final TerritoryManager territoryManager;
    private final ClansPlugin plugin;
    private final PlayerDataManager playerDataManager;

    public FlagListener(TerritoryManager territoryManager, ClansPlugin plugin) {
        this.territoryManager = territoryManager;
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Защита баннера флага
        if (event.getBlock().getState() instanceof Banner) {
            Location flagLoc = event.getBlock().getLocation();
            String clanName = territoryManager.getFlagOwner(flagLoc);
            if (clanName != null) {
                event.setCancelled(true);
                Player attacker = event.getPlayer();
                event.getPlayer().sendMessage(ChatColor.RED + "Флаги нельзя разрушать напрямую! Атакуйте их, чтобы захватить территорию.");
                // Формируем сообщение для кланового чата
                String flagCoords = formatFlagCoordinates(flagLoc);
                String message = ChatColor.RED + "[Флаг] " + attacker.getName() +
                        " атакует наш флаг на координатах: " + flagCoords;
                // Включаем режим клан-чата для системного сообщения
                boolean originalState = playerDataManager.isClanChatSendMode("SYSTEM_FLAG_ALERT");
                playerDataManager.setClanChatSendMode("SYSTEM_FLAG_ALERT", true);
                // Отправляем сообщение в клановый чат
                for (Player member : plugin.getServer().getOnlinePlayers()) {
                    if (clanName.equals(playerDataManager.getPlayerClan(member.getName()))) {
                        member.chat(message);
                    }
                }
                // Возвращаем исходное состояние (хотя для SYSTEM_FLAG_ALERT это не нужно)
                playerDataManager.setClanChatSendMode("SYSTEM_FLAG_ALERT", originalState);
            }
        }

    }
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            if (armorStand.getCustomName() != null && armorStand.getCustomName().contains("Флаг")) {
                if (event.getDamager() instanceof Player) {
                    Player attacker = (Player) event.getDamager();
                    Location flagLocation = armorStand.getLocation().getBlock().getLocation();
                    String clanName = territoryManager.getFlagOwner(flagLocation);

                    if (clanName != null) {
                        // Формируем сообщение для кланового чата
                        String flagCoords = formatFlagCoordinates(flagLocation);
                        String message = ChatColor.RED + "[Флаг] " + attacker.getName() +
                                " атакует наш флаг на координатах: " + flagCoords;

                        // Включаем режим клан-чата для системного сообщения
                        boolean originalState = playerDataManager.isClanChatSendMode("SYSTEM_FLAG_ALERT");
                        playerDataManager.setClanChatSendMode("SYSTEM_FLAG_ALERT", true);

                        // Отправляем сообщение в клановый чат
                        for (Player member : plugin.getServer().getOnlinePlayers()) {
                            if (clanName.equals(playerDataManager.getPlayerClan(member.getName()))) {
                                member.chat(message);
                            }
                        }

                        // Возвращаем исходное состояние
                        playerDataManager.setClanChatSendMode("SYSTEM_FLAG_ALERT", originalState);
                    }

                    // Наносим урон флагу
                    territoryManager.damageFlag(flagLocation, attacker, 1);
                    event.setCancelled(true);
                }
            }
        }
    }
    private String formatFlagCoordinates(Location location) {
        return String.format("%d, %d, %d",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }
}