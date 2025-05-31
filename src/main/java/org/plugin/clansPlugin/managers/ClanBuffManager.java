package org.plugin.clansPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.buffs.ClanBuff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanBuffManager {
    private final ClansPlugin plugin;
    private final Map<String, ClanBuff> clanBuffs = new HashMap<>();
    private final Map<String, Long> lastChangeTimes = new HashMap<>();
    private final long cooldownMillis;

    public ClanBuffManager(ClansPlugin plugin, long cooldownMillis) {
        this.plugin = plugin;
        this.cooldownMillis = cooldownMillis;
    }

    public boolean canChangeBuff(String clanId) {
        long last = lastChangeTimes.getOrDefault(clanId, 0L);
        return System.currentTimeMillis() - last >= cooldownMillis;
    }

    public void setClanBuff(String clanId, ClanBuff buff) {
        removeOldEffects(clanId);

        clanBuffs.put(clanId, buff);
        lastChangeTimes.put(clanId, System.currentTimeMillis());
    }

    private void removeOldEffects(String clanId) {
        ClanBuff oldBuff = clanBuffs.get(clanId);
        if (oldBuff == null) return;

        List<Player> clanMembers = getOnlineClanMembers(clanId);

        for (Player member : clanMembers) {
            member.removePotionEffect(oldBuff.getPrimaryEffect());
            member.removePotionEffect(oldBuff.getSecondaryEffect());
        }
    }

    private List<Player> getOnlineClanMembers(String clanId) {
        List<Player> members = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (clanId.equals(plugin.getPlayerDataManager().getPlayerClan(player.getName()))) {
                members.add(player);
            }
        }
        return members;
    }

    public ClanBuff getBuff(String clanId) {
        return clanBuffs.get(clanId);
    }

    public boolean isValidBuff(String buffName) {
        if (buffName == null) return false;
        String lower = buffName.toLowerCase();
        for (ClanBuff buff : ClanBuff.values()) {
            if (buff.name().toLowerCase().equals(lower)) return true;
        }
        return false;
    }

    public ClanBuff getBuffByName(String buffName) {
        if (buffName == null) return null;
        String lower = buffName.toLowerCase();
        for (ClanBuff buff : ClanBuff.values()) {
            if (buff.name().toLowerCase().equals(lower)) return buff;
        }
        return null;
    }
}
