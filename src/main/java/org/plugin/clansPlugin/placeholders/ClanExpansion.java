package org.plugin.clansPlugin.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.plugin.clansPlugin.ClansPlugin;

import java.util.List;

public class ClanExpansion extends PlaceholderExpansion {

    private final ClansPlugin plugin;

    public ClanExpansion(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clan"; // плейсхолдер будет %clan_name%
    }

    @Override
    public String getAuthor() {
        return "Ты";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        String playerName = player.getName();
        String clanName = plugin.getPlayerDataManager().getPlayerClan(playerName);

        if (clanName == null) {
            return "Пока нет";
        }

        switch (identifier.toLowerCase()) {
            case "tag":
                return " [" + clanName + "]";
            case "leader":
                String leader = plugin.getPlayerDataManager().getClanLeader(clanName);
                return leader != null ? leader : "Пока нет";
            case "members":
                List<String> members = plugin.getPlayerDataManager().getClanMembers(clanName);
                return String.valueOf(members.size());
            case "territory":
                List<String> chunks = plugin.getTerritoryManager().getClanChunks(clanName);
                return String.valueOf(chunks.size());
            case "base":
                Location base = plugin.getTerritoryManager().getClanBaseCenter(clanName);
                return base != null
                        ? String.format("X: %d, Y: %d, Z: %d", base.getBlockX(), base.getBlockY(), base.getBlockZ())
                        : "Пока нет";
            default:
                return null;
        }
    }
}
