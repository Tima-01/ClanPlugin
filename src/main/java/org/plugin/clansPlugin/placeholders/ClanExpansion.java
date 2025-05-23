package org.plugin.clansPlugin.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.plugin.clansPlugin.ClansPlugin;

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
        if (identifier.equalsIgnoreCase("tag")) {
            String clan = plugin.getPlayerDataManager().getPlayerClan(player.getName());
            return clan != null ? " [" + clan + "]" : ""; // пустая строка, если нет клана
        }
        return null;
    }
}
