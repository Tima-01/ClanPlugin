package org.plugin.clansPlugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.HashMap;
import java.util.Map;

public class FlagListener implements Listener {
    private final TerritoryManager territoryManager;
    private final ClansPlugin plugin;
    private final PlayerDataManager playerDataManager;

    // Храним информацию о последних уведомлениях для флагов
    private final Map<Location, Integer> lastNotificationHealth = new HashMap<>();
    private final Map<Location, Boolean> firstAttackNotification = new HashMap<>();

    public FlagListener(TerritoryManager territoryManager, ClansPlugin plugin) {
        this.territoryManager = territoryManager;
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Проверка на баннер (флаг)
        if (block.getState() instanceof Banner) {
            Location flagLoc = block.getLocation();
            String clanName = territoryManager.getFlagOwner(flagLoc);
            if (clanName != null) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Флаги нельзя разрушать напрямую! Атакуйте их, чтобы захватить территорию.");
                return;
            }
        }

        // Проверка на обсидиан под флагом
        if (block.getType() == Material.OBSIDIAN) {
            // Проверяем, есть ли баннер над этим обсидианом
            Block bannerBlock = block.getRelative(BlockFace.UP);
            if (bannerBlock.getState() instanceof Banner) {
                Location flagLoc = bannerBlock.getLocation();
                String clanName = territoryManager.getFlagOwner(flagLoc);
                if (clanName != null) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Основание флага защищено! Атакуйте сам флаг, чтобы захватить территорию.");
                    return;
                }
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
                        // Проверяем, не атакует ли игрок свой же флаг
                        if (!clanName.equals(playerDataManager.getPlayerClan(attacker.getName()))) {
                            int currentHealth = territoryManager.getFlagHealth(flagLocation);

                            // Проверяем условия для отправки уведомления
                            if (shouldSendNotification(flagLocation, currentHealth)) {
                                sendFlagAlert(clanName, attacker, flagLocation, currentHealth);
                            }
                        }

                        territoryManager.damageFlag(flagLocation, attacker, 1);
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // Защита флагов от взрывов
        event.blockList().removeIf(block -> {
            if (block.getState() instanceof Banner) {
                return territoryManager.getFlagOwner(block.getLocation()) != null;
            }
            return false;
        });
    }

    private boolean shouldSendNotification(Location flagLocation, int currentHealth) {
        // Первая атака
        if (!firstAttackNotification.containsKey(flagLocation)) {
            firstAttackNotification.put(flagLocation, true);
            lastNotificationHealth.put(flagLocation, currentHealth);
            return true;
        }

        // Каждые 20 потерянных HP
        int lastNotifiedHealth = lastNotificationHealth.getOrDefault(flagLocation, currentHealth);
        if (lastNotifiedHealth - currentHealth >= 20 || currentHealth <= 0) {
            lastNotificationHealth.put(flagLocation, currentHealth);
            return true;
        }

        return false;
    }

    private void sendFlagAlert(String clanName, Player attacker, Location flagLocation, int currentHealth) {
        // Формируем сообщение в стиле кланового чата
        String flagCoords = formatFlagCoordinates(flagLocation);
        String healthInfo = currentHealth > 0 ?
                " (Осталось здоровья: " + currentHealth + "/" + TerritoryManager.getFlagMaxHealth() + ")" :
                " (Флаг уничтожен!)";

        String message = ChatColor.DARK_AQUA + "[Клан: " + clanName + "] " +
                ChatColor.RED + attacker.getName() + " атакует наш флаг на координатах: " + flagCoords + healthInfo;

        // Отправляем сообщение всем членам клана
        for (Player member : plugin.getServer().getOnlinePlayers()) {
            if (clanName.equals(playerDataManager.getPlayerClan(member.getName()))) {
                member.sendMessage(message);
            }
        }

        // Если флаг уничтожен, очищаем данные о нем
        if (currentHealth <= 0) {
            lastNotificationHealth.remove(flagLocation);
            firstAttackNotification.remove(flagLocation);
        }
    }

    private String formatFlagCoordinates(Location location) {
        return String.format("%d, %d, %d",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }
}