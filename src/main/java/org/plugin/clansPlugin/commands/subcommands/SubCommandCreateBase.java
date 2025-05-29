    package org.plugin.clansPlugin.commands.subcommands;

    import org.bukkit.ChatColor;
    import org.bukkit.Location;
    import org.bukkit.entity.Player;
    import org.plugin.clansPlugin.managers.PlayerDataManager;
    import org.plugin.clansPlugin.managers.TerritoryManager;

    import java.util.List;

    public class SubCommandCreateBase implements SubCommand {

        private final PlayerDataManager pdm;
        private final TerritoryManager territoryManager;
        private static final int BASE_TERRITORY_SIZE = 5; // Базовый размер 5x5 (без учёта участников)
        private static final int SIZE_PER_MEMBER = 2;     // +2 чанка за каждого участника (по 1 в каждую сторону)
        private static final int MAX_TERRITORY_SIZE = 16; // Максимальный размер 16x16

        public SubCommandCreateBase(PlayerDataManager pdm, TerritoryManager territoryManager) {
            this.pdm = pdm;
            this.territoryManager = territoryManager;
        }

        @Override
        public String[] getAliases() {
            return new String[]{"createbase"};
        }

        @Override
        public String getUsage() {
            return "/clan createbase";
        }

        @Override
        public String getDescription() {
            return "Создать основную базу клана (только лидер)";
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
                player.sendMessage(ChatColor.RED + "Только лидер может создавать базу.");
                return true;
            }

            if (territoryManager.getClanTerritory(clanName) != null) {
                player.sendMessage(ChatColor.RED + "У этого клана уже есть база.");
                return true;
            }

            // Проверка расстояния от спавна
            Location basePoint = new Location(player.getWorld(), 1340, 68, 300);
            Location loc = player.getLocation();
            if (loc.distance(basePoint) < 1000) {
                player.sendMessage(ChatColor.RED + "База должна быть не ближе 1000 блоков от спавна.");
                return true;
            }

            // Получаем текущее количество участников
            List<String> members = pdm.getClanMembers(clanName);
            int memberCount = members.size();

            // Рассчитываем размер территории
            int territorySize = calculateTerritorySize(memberCount);

            // Проверка пересечения с другими базами
            int chunkX = loc.getChunk().getX();
            int chunkZ = loc.getChunk().getZ();
            if (territoryManager.isOverlapping(chunkX, chunkZ, territorySize)) {
                player.sendMessage(ChatColor.RED + "База слишком близко к базе другого клана.");
                return true;
            }

            // Создаём территорию
            territoryManager.createSquareTerritory(clanName, loc, territorySize);
            player.sendMessage(ChatColor.GREEN + String.format(
                    "База клана создана! Размер: %dx%d чанков (участников: %d)",
                    territorySize, territorySize, memberCount
            ));
            return true;
        }

        private int calculateTerritorySize(int memberCount) {
            // Базовый размер 5x5 + по 1 чанку в каждую сторону за каждого участника
            int size = BASE_TERRITORY_SIZE + (memberCount * SIZE_PER_MEMBER);
            return Math.min(size, MAX_TERRITORY_SIZE);
        }
    }