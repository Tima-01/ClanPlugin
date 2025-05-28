package org.plugin.clansPlugin.managers;

import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerritoryManager {

    private static final int FLAG_MAX_HEALTH = 100;
    private static final double ARMOR_STAND_OFFSET = 0.5;

    private final ClansPlugin plugin;
    private File territoryFile;
    private YamlConfiguration territoryData;

    public TerritoryManager(ClansPlugin plugin) {
        this.plugin = plugin;
        initTerritoryFile();
    }

    public DyeColor getClanBannerColor(String clanName) {
        return switch (clanName.toLowerCase()) {
            case "бугу" -> DyeColor.RED;
            case "саруу" -> DyeColor.GREEN;
            case "кыпчак" -> DyeColor.BLUE;
            case "саяк" -> DyeColor.YELLOW;
            case "сарыбагыш" -> DyeColor.PURPLE;
            default -> DyeColor.WHITE;
        };
    }

    public void initTerritoryFile() {
        territoryFile = new File(plugin.getDataFolder(), "territories.yml");
        if (!territoryFile.exists()) {
            try {
                territoryFile.getParentFile().mkdirs();
                territoryFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        territoryData = YamlConfiguration.loadConfiguration(territoryFile);
    }

    public void saveTerritoryData() {
        try {
            territoryData.save(territoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClanTerritory(String clanName, int minX, int minZ, int maxX, int maxZ) {
        territoryData.set("territories." + clanName, minX + "," + minZ + "," + maxX + "," + maxZ);
        saveTerritoryData();
    }

    public int[] getClanTerritory(String clanName) {
        if (!territoryData.contains("territories." + clanName)) {
            return null;
        }

        String data = territoryData.getString("territories." + clanName);
        if (data == null) return null;

        String[] coords = data.split(",");
        return new int[]{
                Integer.parseInt(coords[0]),
                Integer.parseInt(coords[1]),
                Integer.parseInt(coords[2]),
                Integer.parseInt(coords[3])
        };
    }

    public boolean isInTerritory(String clanName, int chunkX, int chunkZ) {
        int[] territory = getClanTerritory(clanName);
        if (territory == null) return false;

        return chunkX >= territory[0] &&
                chunkX <= territory[2] &&
                chunkZ >= territory[1] &&
                chunkZ <= territory[3];
    }

    public String getClanByChunk(int chunkX, int chunkZ) {
        // Сначала проверяем основные территории
        String clan = getClanByMainTerritory(chunkX, chunkZ);
        if (clan != null) return clan;

        // Затем проверяем территории флагов
        if (territoryData.contains("flags")) {
            for (String clanName : territoryData.getConfigurationSection("flags").getKeys(false)) {
                for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
                    String flagData = territoryData.getString("flags." + clanName + "." + flagId);
                    if (flagData == null) continue;

                    String[] data = flagData.split(",");
                    if (data.length >= 4) {
                        int minX = Integer.parseInt(data[0]);
                        int minZ = Integer.parseInt(data[1]);
                        int maxX = Integer.parseInt(data[2]);
                        int maxZ = Integer.parseInt(data[3]);

                        if (chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ) {
                            return clanName;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getClanByMainTerritory(int chunkX, int chunkZ) {
        if (!territoryData.contains("territories")) return null;

        for (String clanName : territoryData.getConfigurationSection("territories").getKeys(false)) {
            if (isInTerritory(clanName, chunkX, chunkZ)) {
                return clanName;
            }
        }
        return null;
    }

    public void createSquareTerritory(String clanName, Location center, int sideLength) {
        int centerX = center.getBlockX() >> 4;
        int centerZ = center.getBlockZ() >> 4;

        int radius = sideLength / 2;
        int minX = centerX - radius;
        int minZ = centerZ - radius;
        int maxX = centerX + radius;
        int maxZ = centerZ + radius;

        setClanTerritory(clanName, minX, minZ, maxX, maxZ);
    }

    public void deleteClanTerritory(String clanName) {
        territoryData.set("territories." + clanName, null);
        removeAllClanFlags(clanName);
        saveTerritoryData();
    }
    public boolean isOverlapping(int centerX, int centerZ, int sideLength) {
        return isOverlapping(centerX, centerZ, sideLength, null);
    }

    public boolean isOverlapping(int centerX, int centerZ, int sideLength, String excludeClan) {
        if (!territoryData.contains("territories")) return false;

        int radius = sideLength / 2;
        int newMinX = centerX - radius;
        int newMinZ = centerZ - radius;
        int newMaxX = centerX + radius;
        int newMaxZ = centerZ + radius;

        for (String clanName : territoryData.getConfigurationSection("territories").getKeys(false)) {
            if (clanName.equals(excludeClan)) continue;

            int[] territory = getClanTerritory(clanName);
            if (territory == null) continue;

            if (newMaxX >= territory[0] && newMinX <= territory[2] &&
                    newMaxZ >= territory[1] && newMinZ <= territory[3]) {
                return true;
            }
        }
        return false;
    }

    public Location getClanBaseCenter(String clanName) {
        int[] territory = getClanTerritory(clanName);
        if (territory == null) return null;

        int centerX = (territory[0] + territory[2]) / 2;
        int centerZ = (territory[1] + territory[3]) / 2;

        World world = Bukkit.getWorlds().get(0);
        int blockX = centerX * 16 + 8;
        int blockZ = centerZ * 16 + 8;
        int blockY = world.getHighestBlockYAt(blockX, blockZ);

        return new Location(world, blockX, blockY, blockZ);
    }

    public boolean addFlagTerritory(String clanName, Location flagLocation) {
        int flagChunkX = flagLocation.getBlockX() >> 4;
        int flagChunkZ = flagLocation.getBlockZ() >> 4;
        int flagSize = 3; // Территория 3x3 чанка

        // Координаты территории флага
        int flagMinX = flagChunkX - 1;
        int flagMinZ = flagChunkZ - 1;
        int flagMaxX = flagChunkX + 1;
        int flagMaxZ = flagChunkZ + 1;

        // Проверяем пересечение с чужими территориями
        if (isOverlappingWithOtherClans(clanName, flagMinX, flagMinZ, flagMaxX, flagMaxZ)) {
            return false;
        }

        // Создаем визуальные элементы
        String flagId = UUID.randomUUID().toString();
        ArmorStand armorStand = createFlagVisual(flagLocation, clanName, flagId);
        String armorStandId = armorStand.getUniqueId().toString();

        // Сохраняем данные флага
        String flagData = String.join(",",
                String.valueOf(flagMinX),
                String.valueOf(flagMinZ),
                String.valueOf(flagMaxX),
                String.valueOf(flagMaxZ),
                flagLocation.getWorld().getName(),
                String.valueOf(flagLocation.getBlockX()),
                String.valueOf(flagLocation.getBlockY()),
                String.valueOf(flagLocation.getBlockZ()),
                String.valueOf(FLAG_MAX_HEALTH),
                armorStandId
        );

        territoryData.set("flags." + clanName + "." + flagId, flagData);
        saveTerritoryData();

        return true;
    }

    private boolean isOverlappingWithOtherClans(String clanName, int minX, int minZ, int maxX, int maxZ) {
        // Проверяем основные территории других кланов
        if (territoryData.contains("territories")) {
            for (String otherClan : territoryData.getConfigurationSection("territories").getKeys(false)) {
                if (otherClan.equals(clanName)) continue; // Пропускаем свои территории

                int[] otherTerritory = getClanTerritory(otherClan);
                if (otherTerritory == null) continue;

                if (maxX >= otherTerritory[0] && minX <= otherTerritory[2] &&
                        maxZ >= otherTerritory[1] && minZ <= otherTerritory[3]) {
                    return true; // Найдено пересечение с чужой территорией
                }
            }
        }

        // Проверяем флаги других кланов
        if (territoryData.contains("flags")) {
            for (String otherClan : territoryData.getConfigurationSection("flags").getKeys(false)) {
                if (otherClan.equals(clanName)) continue; // Пропускаем свои флаги

                ConfigurationSection clanFlags = territoryData.getConfigurationSection("flags." + otherClan);
                for (String flagId : clanFlags.getKeys(false)) {
                    String flagData = territoryData.getString("flags." + otherClan + "." + flagId);
                    if (flagData == null) continue;

                    String[] coords = flagData.split(",");
                    if (coords.length >= 4) {
                        int otherMinX = Integer.parseInt(coords[0]);
                        int otherMinZ = Integer.parseInt(coords[1]);
                        int otherMaxX = Integer.parseInt(coords[2]);
                        int otherMaxZ = Integer.parseInt(coords[3]);

                        if (maxX >= otherMinX && minX <= otherMaxX &&
                                maxZ >= otherMinZ && minZ <= otherMaxZ) {
                            return true; // Найдено пересечение с чужим флагом
                        }
                    }
                }
            }
        }

        return false;
    }

    private ArmorStand createFlagVisual(Location location, String clanName, String flagId) {
        DyeColor color = getClanBannerColor(clanName);

        // Убедимся, что координаты блока целочисленные
        Location blockCenter = location.getBlock().getLocation().add(0.5, 0, 0.5);

        // Устанавливаем баннер
//        Block block = blockCenter.getBlock();
//        block.setType(Material.valueOf(color.name() + "_BANNER"));
//
//        // Настраиваем баннер
//        Banner banner = (Banner) block.getState();
//        banner.setBaseColor(color);
//        banner.update(true);

        // Создаем ArmorStand для отображения здоровья
        Location armorStandLoc = blockCenter.clone().add(0, 0, 0);
        ArmorStand armorStand = blockCenter.getWorld().spawn(armorStandLoc, ArmorStand.class);

        armorStand.setVisible(false);
        armorStand.setInvulnerable(false);
        armorStand.setCustomName(getHealthDisplay(clanName, FLAG_MAX_HEALTH));
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setMarker(false);
        armorStand.setSmall(false);

        return armorStand;
    }

    private String getHealthDisplay(String clanName, int health) {
        ChatColor color = health > 50 ? ChatColor.GREEN : health > 20 ? ChatColor.YELLOW : ChatColor.RED;
        return ChatColor.WHITE + "Флаг " + clanName + " " +
                ChatColor.GRAY + "[" + color + health + "❤" + ChatColor.GRAY + "]";
    }

    public String getFlagOwner(Location location) {
        if (!territoryData.contains("flags")) {
            return null;
        }

        for (String clanName : territoryData.getConfigurationSection("flags").getKeys(false)) {
            ConfigurationSection clanFlags = territoryData.getConfigurationSection("flags." + clanName);
            for (String flagId : clanFlags.getKeys(false)) {
                String flagData = territoryData.getString("flags." + clanName + "." + flagId);
                if (flagData == null) continue;

                String[] data = flagData.split(",");
                if (data.length >= 8) { // Проверяем новый формат с health
                    int x = Integer.parseInt(data[5]);
                    int y = Integer.parseInt(data[6]);
                    int z = Integer.parseInt(data[7]);

                    if (x == location.getBlockX() &&
                            y == location.getBlockY() &&
                            z == location.getBlockZ()) {
                        return clanName;
                    }
                }
            }
        }
        return null;
    }

    private String findFlagId(Location location) {
        if (!territoryData.contains("flags")) {
            return null;
        }

        for (String clanName : territoryData.getConfigurationSection("flags").getKeys(false)) {
            ConfigurationSection clanFlags = territoryData.getConfigurationSection("flags." + clanName);
            for (String flagId : clanFlags.getKeys(false)) {
                String flagData = territoryData.getString("flags." + clanName + "." + flagId);
                if (flagData == null) continue;

                String[] data = flagData.split(",");
                if (data.length >= 8) {
                    int x = Integer.parseInt(data[5]);
                    int y = Integer.parseInt(data[6]);
                    int z = Integer.parseInt(data[7]);

                    if (x == location.getBlockX() &&
                            y == location.getBlockY() &&
                            z == location.getBlockZ()) {
                        return flagId;
                    }
                }
            }
        }
        return null;
    }

    public boolean damageFlag(Location flagLocation, Player damager, int damage) {
        String clanOwner = getFlagOwner(flagLocation);
        if (clanOwner == null) return false;

        String attackerClan = plugin.getPlayerDataManager().getPlayerClan(damager.getName());
        if (clanOwner.equals(attackerClan)) {
            damager.sendMessage(ChatColor.RED + "Вы не можете атаковать флаги своего клана!");
            return false;
        }

        String flagId = findFlagId(flagLocation);
        if (flagId == null) return false;

        String flagData = territoryData.getString("flags." + clanOwner + "." + flagId);
        if (flagData == null) return false;

        String[] data = flagData.split(",");
        if (data.length < 9) return false; // Проверяем наличие всех данных

        int currentHealth = Integer.parseInt(data[8]);
        int newHealth = currentHealth - damage;

        if (newHealth <= 0) {
            destroyFlag(clanOwner, flagId);
            Bukkit.broadcastMessage(ChatColor.RED + "Флаг клана " + clanOwner + " был уничтожен игроком " + damager.getName() + "!");
            return true;
        } else {
            // Обновляем здоровье в строке данных
            data[8] = String.valueOf(newHealth);
            territoryData.set("flags." + clanOwner + "." + flagId, String.join(",", data));
            saveTerritoryData();

            updateFlagHealthDisplay(clanOwner, flagId, newHealth);
            damager.sendMessage(ChatColor.YELLOW + "Вы повредили флаг! Осталось здоровья: " + newHealth + "/" + FLAG_MAX_HEALTH);
            return true;
        }
    }

    private void updateFlagHealthDisplay(String clanName, String flagId, int health) {
        String flagData = territoryData.getString("flags." + clanName + "." + flagId);
        if (flagData == null) return;

        String[] data = flagData.split(",");
        if (data.length >= 10) { // Проверяем наличие armorStand UUID
            UUID armorStandId = UUID.fromString(data[9]);

            Bukkit.getScheduler().runTask(plugin, () -> {
                ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(armorStandId);
                if (armorStand != null && armorStand.isValid()) {
                    String healthText = (health > 50 ? "§a" : health > 20 ? "§e" : "§c") + health + "❤";
                    armorStand.setCustomName("&fФлаг " + clanName + " §7[" + healthText + "§7]");
                }
            });
        }
    }

    private void destroyFlag(String clanName, String flagId) {
        removeFlagVisuals(clanName, flagId);
        territoryData.set("flags." + clanName + "." + flagId, null);
        saveTerritoryData();
    }

    private void removeFlagVisuals(String clanName, String flagId) {
        String flagData = territoryData.getString("flags." + clanName + "." + flagId);
        if (flagData == null) return;

        String[] data = flagData.split(",");
        if (data.length >= 10) { // Проверяем наличие armorStand UUID
            // Удаляем ArmorStand
            UUID armorStandId = UUID.fromString(data[9]);
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(armorStandId);
            if (armorStand != null) armorStand.remove();
        }

        if (data.length >= 8) {
            // Удаляем баннер
            World world = Bukkit.getWorld(data[4]);
            if (world != null) {
                Location loc = new Location(
                        world,
                        Integer.parseInt(data[5]),
                        Integer.parseInt(data[6]),
                        Integer.parseInt(data[7])
                );
                loc.getBlock().setType(Material.AIR);
            }
        }
    }

    public boolean removeClanFlag(Location flagLocation) {
        if (!territoryData.contains("flags")) {
            return false;
        }

        for (String clanName : territoryData.getConfigurationSection("flags").getKeys(false)) {
            ConfigurationSection clanFlags = territoryData.getConfigurationSection("flags." + clanName);
            for (String flagId : clanFlags.getKeys(false)) {
                String flagData = territoryData.getString("flags." + clanName + "." + flagId);
                if (flagData == null) continue;

                String[] data = flagData.split(",");
                if (data.length >= 8) { // Проверяем новый формат
                    int x = Integer.parseInt(data[5]);
                    int y = Integer.parseInt(data[6]);
                    int z = Integer.parseInt(data[7]);

                    if (x == flagLocation.getBlockX() &&
                            y == flagLocation.getBlockY() &&
                            z == flagLocation.getBlockZ()) {
                        // Удаляем визуальные элементы
                        removeFlagVisuals(clanName, flagId);
                        // Удаляем данные флага
                        territoryData.set("flags." + clanName + "." + flagId, null);
                        saveTerritoryData();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<int[]> getAllClanTerritories(String clanName) {
        List<int[]> territories = new ArrayList<>();
        int[] mainTerritory = getClanTerritory(clanName);
        if (mainTerritory != null) {
            territories.add(mainTerritory);
        }

        if (territoryData.contains("flags." + clanName)) {
            for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
                String flagData = territoryData.getString("flags." + clanName + "." + flagId);
                if (flagData == null) continue;

                String[] coords = flagData.split(",");
                if (coords.length >= 4) {
                    territories.add(new int[]{
                            Integer.parseInt(coords[0]),
                            Integer.parseInt(coords[1]),
                            Integer.parseInt(coords[2]),
                            Integer.parseInt(coords[3])
                    });
                }
            }
        }
        return territories;
    }
    public void removeAllClanFlags(String clanName) {
        if (!territoryData.contains("flags." + clanName)) {
            return;
        }

        // Получаем все флаги клана
        ConfigurationSection clanFlags = territoryData.getConfigurationSection("flags." + clanName);
        if (clanFlags == null) return;

        // Создаем копию ключей, чтобы избежать ConcurrentModificationException
        List<String> flagIds = new ArrayList<>(clanFlags.getKeys(false));

        for (String flagId : flagIds) {
            // Удаляем визуальные элементы и данные каждого флага
            removeFlagVisuals(clanName, flagId);
        }

        // Удаляем всю секцию флагов клана
        territoryData.set("flags." + clanName, null);
        saveTerritoryData();
    }
}