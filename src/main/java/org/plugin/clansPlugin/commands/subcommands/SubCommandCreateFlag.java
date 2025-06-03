package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Banner;
import org.bukkit.DyeColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class SubCommandCreateFlag implements SubCommand {

    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;
    private final Economy economy;
    private final double flagCost; // Стоимость установки флага

    public SubCommandCreateFlag(PlayerDataManager pdm, TerritoryManager territoryManager, Economy economy, double flagCost) {
        this.pdm = pdm;
        this.territoryManager = territoryManager;
        this.economy = economy;
        this.flagCost = flagCost;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"createflag"};
    }

    @Override
    public String getUsage() {
        return "/clan createflag";
    }

    @Override
    public String getDescription() {
        return "Установить флаг для расширения территории (только лидер). Стоимость: " + flagCost + "$";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String playerName = player.getName();
        String clanName = pdm.getPlayerClan(playerName);
        if (clanName == null) {
            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
            return true;
        }

        String leader = pdm.getClanLeader(clanName);
        if (!playerName.equalsIgnoreCase(leader)) {
            player.sendMessage(ChatColor.RED + "Только лидер клана может устанавливать флаги.");
            return true;
        }

        if (territoryManager.getClanTerritory(clanName) == null) {
            player.sendMessage(ChatColor.RED + "Ваш клан должен иметь основную базу перед установкой флагов.");
            return true;
        }

        // Проверка баланса
        if (!economy.has(player, flagCost)) {
            player.sendMessage(ChatColor.RED + "Недостаточно денег для установки флага. Нужно: " +
                    ChatColor.YELLOW + flagCost + "сом" +
                    ChatColor.RED + ", у вас: " +
                    ChatColor.YELLOW + economy.getBalance(player) + "сом");
            return true;
        }

        Location flagLocation = player.getLocation();
        Block below = flagLocation.getBlock().getRelative(BlockFace.DOWN);
        if (!below.getType().isSolid() || below.getType() == Material.WATER || below.getType() == Material.LAVA) {
            player.sendMessage(ChatColor.RED + "Флаг можно установить только на твердую поверхность (не воду/лаву).");
            return true;
        }

        boolean success = territoryManager.addFlagTerritory(clanName, flagLocation);
        if (success) {
            // Списываем деньги
            economy.withdrawPlayer(player, flagCost);

            player.sendMessage(ChatColor.GREEN + "Флаг успешно установлен! Создана новая территория 3×3 чанка.");
            player.sendMessage(ChatColor.GREEN + "С вашего счета списано: " + ChatColor.YELLOW + flagCost + "сом.");

            DyeColor clanColor = territoryManager.getClanBannerColor(clanName);

            flagLocation.getBlock().setType(Material.valueOf(clanColor.name() + "_BANNER"));
            Block banner = flagLocation.getBlock();
            Banner bannerState = (Banner) banner.getState();
            bannerState.setBaseColor(clanColor);
            bannerState.update(true);

            player.getWorld().playSound(flagLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.getWorld().spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    flagLocation.clone().add(0.5, 1.5, 0.5),
                    30, 0.5, 0.5, 0.5, 0.1
            );
        } else {
            String clanAtLocation = territoryManager.getClanByChunk(
                    flagLocation.getBlockX() >> 4,
                    flagLocation.getBlockZ() >> 4
            );

            if (clanAtLocation != null && !clanAtLocation.equals(clanName)) {
                player.sendMessage(ChatColor.RED + "Эта территория уже принадлежит клану " + clanAtLocation);
            } else {
                player.sendMessage(ChatColor.RED + "Невозможно установить флаг здесь. Возможные причины:");
                player.sendMessage(ChatColor.RED + "- Точка не примыкает к вашей территории");
                player.sendMessage(ChatColor.RED + "- Точка пересекается с территорией другого клана");
                player.sendMessage(ChatColor.RED + "- Недостаточно места для установки флага");
            }
        }
        return true;
    }
}