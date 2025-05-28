package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Banner;
import org.bukkit.DyeColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

public class SubCommandCreateFlag implements SubCommand {

    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;

    public SubCommandCreateFlag(PlayerDataManager pdm, TerritoryManager territoryManager) {
        this.pdm = pdm;
        this.territoryManager = territoryManager;
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
        return "Установить флаг для расширения территории (только лидер)";
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

        Location flagLocation = player.getLocation();
        // Проверяем, что блок под ногами подходит для установки флага
        if (!flagLocation.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
            player.sendMessage(ChatColor.RED + "Флаг можно установить только на твердую поверхность.");
            return true;
        }

        boolean success = territoryManager.addFlagTerritory(clanName, flagLocation);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "Флаг успешно установлен! Создана новая территория 3×3 чанка.");

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
            player.sendMessage(ChatColor.RED + "Невозможно установить флаг здесь. Возможные причины:");
            player.sendMessage(ChatColor.RED + "- Точка не примыкает к вашей территории");
            player.sendMessage(ChatColor.RED + "- Точка пересекается с территорией другого клана");
        }
        return true;
    }
}
