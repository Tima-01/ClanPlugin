package org.plugin.clansPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class VoteManager {

    private final JavaPlugin plugin;
    private final PlayerDataManager playerDataManager;

    // Клан -> Map<Кандидат, количество голосов>
    private final Map<String, Map<String, Integer>> voteData = new HashMap<>();

    // Клан -> Map<Игрок, за кого проголосовал>
    private final Map<String, Map<String, String>> playerVotes = new HashMap<>();

    // Клан -> Время окончания голосования
    private final Map<String, LocalDateTime> voteEndTimes = new HashMap<>();

    public VoteManager(JavaPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;

        // Планируем проверку окончаний голосования каждый 5 минут
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkVoteEndings, 0L, 20L * 60 * 5);
    }

    /**
     * Администратор запускает голосование для конкретного клана
     */
    public void startVote(String clanName) {
        if (voteData.containsKey(clanName)) {
            Bukkit.broadcastMessage(ChatColor.RED + "Голосование в клане " + clanName + " уже идет.");
            return;
        }

        List<String> members = playerDataManager.getClanMembers(clanName);
        if (members.size() < 2) {
            Bukkit.broadcastMessage(ChatColor.RED + "Недостаточно участников для голосования в клане " + clanName + ".");
            return;
        }

        voteData.put(clanName, new HashMap<>());
        playerVotes.put(clanName, new HashMap<>());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.with(LocalTime.MIDNIGHT);
        voteEndTimes.put(clanName, end);

        for (String member : members) {
            Player p = Bukkit.getPlayerExact(member);
            if (p != null && p.isOnline()) {
                p.sendMessage(ChatColor.GOLD + "В клане " + clanName + " началось голосование за нового лидера! Проголосуй: /votel <ник>");
            }
        }
    }
    public void forceEndVote(String clanName) {
        if (!voteData.containsKey(clanName)) {
            Bukkit.broadcastMessage(ChatColor.RED + "В клане " + clanName + " нет активного голосования.");
            return;
        }

        endVote(clanName); // Завершаем голосование (назначаем лидера и сообщаем)

        // Удаляем все данные голосования по клану
        voteData.remove(clanName);
        playerVotes.remove(clanName);
        voteEndTimes.remove(clanName);

        Bukkit.broadcastMessage(ChatColor.GOLD + "Администратор досрочно завершил голосование в клане " + clanName + ".");
    }
    /**
     * Игрок голосует за другого члена клана
     */
    public void vote(String voterName, String targetName) {
        String clanName = playerDataManager.getPlayerClan(voterName);
        if (clanName == null || !clanName.equals(playerDataManager.getPlayerClan(targetName))) {
            Player p = Bukkit.getPlayerExact(voterName);
            if (p != null) {
                p.sendMessage(ChatColor.RED + "Вы можете голосовать только за участников вашего клана.");
            }
            return;
        }

        if (!voteData.containsKey(clanName)) {
            Player p = Bukkit.getPlayerExact(voterName);
            if (p != null) {
                p.sendMessage(ChatColor.RED + "Сейчас нет активного голосования.");
            }
            return;
        }

        if (playerVotes.get(clanName).containsKey(voterName)) {
            Player p = Bukkit.getPlayerExact(voterName);
            if (p != null) {
                p.sendMessage(ChatColor.RED + "Вы уже проголосовали.");
            }
            return;
        }

        Map<String, Integer> votes = voteData.get(clanName);
        votes.put(targetName, votes.getOrDefault(targetName, 0) + 1);
        playerVotes.get(clanName).put(voterName, targetName);

        Player p = Bukkit.getPlayerExact(voterName);
        if (p != null) {
            p.sendMessage(ChatColor.GREEN + "Вы проголосовали за " + targetName + ".");
        }
    }

    /**
     * Проверяет, нужно ли завершить голосование
     */
    private void checkVoteEndings() {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, LocalDateTime> entry : voteEndTimes.entrySet()) {
            String clan = entry.getKey();
            if (LocalDateTime.now().isAfter(entry.getValue())) {
                endVote(clan);
                toRemove.add(clan);
            }
        }
        for (String clan : toRemove) {
            voteData.remove(clan);
            playerVotes.remove(clan);
            voteEndTimes.remove(clan);
        }
    }

    /**
     * Завершает голосование и назначает нового лидера
     */
    private void endVote(String clanName) {
        Map<String, Integer> votes = voteData.get(clanName);
        if (votes == null || votes.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Голосование в клане " + clanName + " завершено. Никто не был выбран.");
            return;
        }

        String newLeader = votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();

        playerDataManager.setClanLeader(clanName, newLeader);

        Bukkit.broadcastMessage(ChatColor.GREEN + "Голосование в клане " + clanName + " завершено! Новый лидер: " + newLeader);
    }

    /**
     * Проверяет, идет ли голосование
     */
    public boolean isVotingActive(String clanName) {
        return voteData.containsKey(clanName);
    }
}
