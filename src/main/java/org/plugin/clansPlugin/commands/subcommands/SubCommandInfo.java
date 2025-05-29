    package org.plugin.clansPlugin.commands.subcommands;
    
    import me.clip.placeholderapi.PlaceholderAPI;
    import org.bukkit.ChatColor;
    import org.bukkit.Location;
    import org.bukkit.entity.Player;
    import org.plugin.clansPlugin.ClansPlugin;
    import org.plugin.clansPlugin.managers.PlayerDataManager;
    import org.plugin.clansPlugin.managers.TerritoryManager;
    import org.bukkit.configuration.file.YamlConfiguration;
    
    import java.io.File;
    import java.util.*;
    
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
            YamlConfiguration territoriesConfig = loadTerritoriesConfig();
    
            String message = ChatColor.GOLD + "=====[ Инфо о клане ]=====\n" +
                    ChatColor.YELLOW + "Клан: " + ChatColor.AQUA + "%clan_tag%\n" +
                    ChatColor.YELLOW + "Лидер: " + ChatColor.LIGHT_PURPLE + "%clan_leader:" + clanName + "%\n" +
                    ChatColor.YELLOW + "Участники (" + ChatColor.AQUA + "%clan_members:" + clanName + "%" + ChatColor.YELLOW + "):\n" +
                    getMembersList(clanName) + "\n" +
                    ChatColor.YELLOW + "База: " + getBaseInfo(clanName) + "\n" +
                    getTerritoryInfo(clanName, territoriesConfig) + "\n" +
                    ChatColor.GOLD + "=========================";
    
            message = PlaceholderAPI.setPlaceholders(player, message);
            player.sendMessage(message.split("\n"));
    
            return true;
        }
    
        private YamlConfiguration loadTerritoriesConfig() {
            File file = new File(ClansPlugin.getInstance().getDataFolder(), "territories.yml");
            if (!file.exists()) {
                ClansPlugin.getInstance().saveResource("territories.yml", false);
            }
            return YamlConfiguration.loadConfiguration(file);
        }
    
        private String getMembersList(String clanName) {
            StringBuilder membersList = new StringBuilder();
            for (String member : pdm.getClanMembers(clanName)) {
                membersList.append(ChatColor.GRAY).append("- ").append(member).append("\n");
            }
            return membersList.toString().trim();
        }
    
        private String getBaseInfo(String clanName) {
            Location base = territoryManager.getClanBaseCenter(clanName);
            if (base != null) {
                return ChatColor.AQUA + base.getWorld().getName() + " [" +
                        base.getBlockX() + ", " + base.getBlockY() + ", " + base.getBlockZ() + "]";
            }
            return ChatColor.RED + "не установлена";
        }
    
        private String getTerritoryInfo(String clanName, YamlConfiguration territoriesConfig) {
            Set<String> territoryChunks = getUniqueTerritoryChunks(clanName, territoriesConfig);
    
            if (territoryChunks.isEmpty()) {
                return ChatColor.YELLOW + "Территория: " + ChatColor.RED + "отсутствует";
            }
    
            int chunkCount = territoryChunks.size();
            return ChatColor.YELLOW + "Территория: " + ChatColor.AQUA + chunkCount + " чанков";
        }
    
        private Set<String> getUniqueTerritoryChunks(String clanName, YamlConfiguration territoriesConfig) {
            Set<String> chunks = new HashSet<>();
    
            // Обрабатываем базу
            String baseCoords = territoriesConfig.getString("territories." + clanName);
            if (baseCoords != null) {
                addChunksFromCoords(chunks, baseCoords);
            }
    
            // Обрабатываем флаги
            if (territoriesConfig.contains("flags." + clanName)) {
                territoriesConfig.getConfigurationSection("flags." + clanName)
                        .getKeys(false)
                        .forEach(flagId -> {
                            String flagData = territoriesConfig.getString("flags." + clanName + "." + flagId);
                            if (flagData != null) {
                                String[] parts = flagData.split(",");
                                if (parts.length >= 4) {
                                    addChunksFromCoords(chunks, parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3]);
                                }
                            }
                        });
            }
    
            return chunks;
        }
    
        private void addChunksFromCoords(Set<String> chunks, String coords) {
            String[] parts = coords.split(",");
            if (parts.length == 4) {
                try {
                    int x1 = Integer.parseInt(parts[0]);
                    int z1 = Integer.parseInt(parts[1]);
                    int x2 = Integer.parseInt(parts[2]);
                    int z2 = Integer.parseInt(parts[3]);
    
                    int minX = Math.min(x1, x2);
                    int maxX = Math.max(x1, x2);
                    int minZ = Math.min(z1, z2);
                    int maxZ = Math.max(z1, z2);
    
                    for (int x = minX; x <= maxX; x++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            chunks.add(x + "," + z);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Логирование ошибки при необходимости
                }
            }
        }
    }