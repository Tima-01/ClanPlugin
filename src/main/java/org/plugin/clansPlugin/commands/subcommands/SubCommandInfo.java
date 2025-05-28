package org.plugin.clansPlugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.List;

public class SubCommandInfo implements SubCommand {

    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;

    public SubCommandInfo(PlayerDataManager pdm, TerritoryManager territoryManager) {
        this.pdm = pdm;
        this.territoryManager = territoryManager;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"info"};
    }

    @Override
    public String getUsage() {
        return "/clan info";
    }

    @Override
    public String getDescription() {
        return "Показывает информацию о вашем клане";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Использование: " + getUsage());
            return false;
        }

        String playerName = player.getName();
        if (!pdm.isPlayerInClan(playerName)) {
            player.sendMessage(ChatColor.RED + "Ты ещё не в клане.");
            return true;
        }

        String clanName = pdm.getPlayerClan(playerName);
        List<String> members = pdm.getClanMembers(clanName);
        String leader = pdm.getClanLeader(clanName);

        player.sendMessage(ChatColor.GOLD + "=====[ Инфо о клане ]=====");
        player.sendMessage(ChatColor.YELLOW + "Клан: " + ChatColor.AQUA + clanName);
        if (leader != null) {
            player.sendMessage(ChatColor.YELLOW + "Лидер: " + ChatColor.LIGHT_PURPLE + leader);
        }
        player.sendMessage(ChatColor.YELLOW + "Участники:");
        for (String member : members) {
            player.sendMessage(ChatColor.GRAY + "- " + member);
        }

        // Информация о базе
        Location base = territoryManager.getClanBaseCenter(clanName);
        if (base != null) {
            String coords = base.getWorld().getName() + " [" + base.getBlockX() + ", " + base.getBlockY() + ", " + base.getBlockZ() + "]";
            player.sendMessage(ChatColor.YELLOW + "База: " + ChatColor.AQUA + coords);

            // Расчёт размера территории (6×6 плюс +1 блок за каждого участника)
            int baseSideLength = 6;
            int memberCount = members.size();
            int sideLength = baseSideLength + memberCount; // один участник = +1 чанков к каждой стороне
            int totalChunks = sideLength * sideLength;
            player.sendMessage(ChatColor.YELLOW + "Размер территории: "
                    + ChatColor.AQUA + sideLength + "×" + sideLength
                    + ChatColor.GRAY + " (" + totalChunks + " чанков)");
        } else {
            player.sendMessage(ChatColor.YELLOW + "База: " + ChatColor.RED + "не установлена");
        }

        player.sendMessage(ChatColor.GOLD + "=========================");
        return true;
    }
}
