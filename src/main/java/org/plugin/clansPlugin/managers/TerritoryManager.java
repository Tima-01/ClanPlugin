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

    private DyeColor getClanBannerColor(String clanName) {
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

    /**
     * Сохраняет территорию клана в виде квадрата (4 угловые точки)
     * Формат: minX,minZ,maxX,maxZ
     */
    public void setClanTerritory(String clanName, int minX, int minZ, int maxX, int maxZ) {
        territoryData.set("territories." + clanName, minX + "," + minZ + "," + maxX + "," + maxZ);
        saveTerritoryData();
    }

    /**
     * Получает границы территории клана
     * @return массив [minX, minZ, maxX, maxZ] или null если нет территории
     */
    public int[] getClanTerritory(String clanName) {
        if (!territoryData.contains("territories." + clanName)) {
            return null;
        }

        String[] coords = territoryData.getString("territories." + clanName).split(",");
        return new int[]{
                Integer.parseInt(coords[0]),
                Integer.parseInt(coords[1]),
                Integer.parseInt(coords[2]),
                Integer.parseInt(coords[3])
        };
    }

    /**
     * Проверяет, находится ли чанк на территории клана
     */
    public boolean isInTerritory(String clanName, int chunkX, int chunkZ) {
        int[] territory = getClanTerritory(clanName);
        if (territory == null) return false;

        return chunkX >= territory[0] &&
                chunkX <= territory[2] &&
                chunkZ >= territory[1] &&
                chunkZ <= territory[3];
    }

    /**
     * Получает клан, которому принадлежит чанк
     */
    public String getClanByChunk(int chunkX, int chunkZ) {
        // Сначала проверяем основные территории
        String clan = getClanByMainTerritory(chunkX, chunkZ);
        if (clan != null) return clan;

        // Затем проверяем территории флагов
        if (territoryData.contains("flags")) {
            for (String clanName : territoryData.getConfigurationSection("flags").getKeys(false)) {
                for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
                    String[] data = territoryData.getString("flags." + clanName + "." + flagId).split(",");
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

    /**
     * Рассчитывает границы территории на основе центра и количества чанков
     */
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

    /**
     * Удаляет территорию клана
     */
    public void deleteClanTerritory(String clanName) {
        territoryData.set("territories." + clanName, null);
        saveTerritoryData();
    }

    /**
     * Проверяет пересечение территорий
     */
    public boolean isOverlapping(int centerX, int centerZ, int sideLength) {
        return isOverlapping(centerX, centerZ, sideLength, null);
    }

    /**
     * Проверяет пересечение территорий (с исключением определенного клана)
     */
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

            // Проверка пересечения прямоугольников
            if (newMaxX >= territory[0] && newMinX <= territory[2] &&
                    newMaxZ >= territory[1] && newMinZ <= territory[3]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Получает центр территории клана
     */
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

    /**
     * Пытается добавить флаг (3x3 чанка) к территории клана
     * @param flagLocation Местоположение флага (блок)
     * @param clanName Название клана
     * @return true если флаг был успешно добавлен, false если невозможно
     */

    public boolean addFlagTerritory(String clanName, Location flagLocation) {
        int flagChunkX = flagLocation.getBlockX() >> 4;
        int flagChunkZ = flagLocation.getBlockZ() >> 4;
        int flagSize = 3;

        if (isOverlapping(flagChunkX, flagChunkZ, flagSize, clanName) ||
                !isAdjacentToClanTerritory(clanName, flagChunkX-1, flagChunkZ-1, flagChunkX+1, flagChunkZ+1)) {
            return false;
        }

        String flagId = UUID.randomUUID().toString();
        String flagKey = "flags." + clanName + "." + flagId;

        // Сохраняем данные флага
        territoryData.set(flagKey + ".territory",
                (flagChunkX-1) + "," + (flagChunkZ-1) + "," +
                        (flagChunkX+1) + "," + (flagChunkZ+1));
        territoryData.set(flagKey + ".location",
                flagLocation.getBlockX() + "," +
                        flagLocation.getBlockY() + "," +
                        flagLocation.getBlockZ());
        territoryData.set(flagKey + ".health", FLAG_MAX_HEALTH);
        territoryData.set(flagKey + ".world", flagLocation.getWorld().getName());

        saveTerritoryData();

        // Создаем визуал флага
        createFlagVisual(flagLocation, clanName, flagId);
        return true;
    }

    // Метод для создания визуала флага
    private void createFlagVisual(Location location, String clanName, String flagId) {
        DyeColor color = getClanBannerColor(clanName);

        // Устанавливаем баннер
        Block block = location.getBlock();
        block.setType(Material.valueOf(color.name() + "_BANNER"));

        // Настраиваем баннер
        Banner banner = (Banner) block.getState();
        banner.setBaseColor(color);
        banner.update(true);

        // Создаем индикатор здоровья
        Location armorStandLoc = location.clone().add(ARMOR_STAND_OFFSET, ARMOR_STAND_OFFSET, ARMOR_STAND_OFFSET);
        ArmorStand armorStand = location.getWorld().spawn(armorStandLoc, ArmorStand.class);

        armorStand.setVisible(false);
        armorStand.setInvulnerable(true);
        armorStand.setCustomName(getHealthDisplay(clanName, FLAG_MAX_HEALTH));
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setMarker(true);

        // Сохраняем ID ArmorStand
        territoryData.set("flags." + clanName + "." + flagId + ".armorStand", armorStand.getUniqueId().toString());
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

        // Проходим по всем флагам всех кланов
        for (String clanName : territoryData.getConfigurationSection("flags").getKeys(false)) {
            ConfigurationSection clanFlags = territoryData.getConfigurationSection("flags." + clanName);
            for (String flagId : clanFlags.getKeys(false)) {
                String[] locData = territoryData.getString("flags." + clanName + "." + flagId + ".location").split(",");
                if (locData.length == 3) {
                    int x = Integer.parseInt(locData[0]);
                    int y = Integer.parseInt(locData[1]);
                    int z = Integer.parseInt(locData[2]);

                    // Сравниваем координаты
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
                String[] locData = territoryData.getString("flags." + clanName + "." + flagId + ".location").split(",");
                if (locData.length == 3) {
                    int x = Integer.parseInt(locData[0]);
                    int y = Integer.parseInt(locData[1]);
                    int z = Integer.parseInt(locData[2]);

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

        // Проверка на соклановца
        if (clanOwner.equals(plugin.getPlayerDataManager().getPlayerClan(damager.getName()))) {
            damager.sendMessage(ChatColor.RED + "Вы не можете атаковать флаги своего клана!");
            return false;
        }

        String flagId = findFlagId(flagLocation);
        if (flagId == null) return false;

        int currentHealth = territoryData.getInt("flags." + clanOwner + "." + flagId + ".health", FLAG_MAX_HEALTH);
        int newHealth = currentHealth - damage;

        if (newHealth <= 0) {
            destroyFlag(clanOwner, flagId);
            Bukkit.broadcastMessage(ChatColor.RED + "Флаг клана " + clanOwner + " был уничтожен!");
            return true;
        } else {
            territoryData.set("flags." + clanOwner + "." + flagId + ".health", newHealth);
            saveTerritoryData();

            updateFlagHealthDisplay(clanOwner, flagId, newHealth);
            damager.sendMessage(ChatColor.YELLOW + "Флаг поврежден! Осталось здоровья: " + newHealth + "/" + FLAG_MAX_HEALTH);
            return true;
        }
    }

    private boolean isAdjacentToClanTerritory(String clanName, int minX, int minZ, int maxX, int maxZ) {
        // Проверяем основную территорию
        int[] mainTerritory = getClanTerritory(clanName);
        if (mainTerritory != null && isAdjacent(mainTerritory, minX, minZ, maxX, maxZ)) {
            return true;
        }

        // Проверяем все флаги клана
        if (territoryData.contains("flags." + clanName)) {
            for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
                String[] coords = territoryData.getString("flags." + clanName + "." + flagId + ".territory").split(",");
                int[] flagTerritory = new int[]{
                        Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]),
                        Integer.parseInt(coords[3])
                };

                if (isAdjacent(flagTerritory, minX, minZ, maxX, maxZ)) {
                    return true;
                }
            }
        }

        return false;
    }

    /* Обновляет отображение здоровья флага
     * @param clanName Название клана-владельца
     * @param flagId ID флага
     * @param health Новое значение здоровья
     */
    private void updateFlagHealthDisplay(String clanName, String flagId, int health) {
        // Получаем UUID ArmorStand из конфига
        String armorStandIdStr = territoryData.getString("flags." + clanName + "." + flagId + ".armorStand");
        if (armorStandIdStr == null) return;

        UUID armorStandId = UUID.fromString(armorStandIdStr);

        // Используем BukkitScheduler для безопасного обновления
        Bukkit.getScheduler().runTask(plugin, () -> {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(armorStandId);
            if (armorStand != null && armorStand.isValid()) {
                // Форматируем текст с цветами и сердечками
                String healthText = (health > 50 ? "§a" : health > 20 ? "§e" : "§c") + health + "❤";
                armorStand.setCustomName("&fФлаг " + clanName + " §7[" + healthText + "§7]");
            }
        });
    }

    private void destroyFlag(String clanName, String flagId) {
        // Удаляем визуальные элементы
        removeFlagVisuals(clanName, flagId);

        // Удаляем территорию флага
        territoryData.set("flags." + clanName + "." + flagId, null);
        saveTerritoryData();
    }

    private void removeFlagVisuals(String clanName, String flagId) {
        // Удаляем ArmorStand
        String armorStandId = territoryData.getString("flags." + clanName + "." + flagId + ".armorStand");
        if (armorStandId != null) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(armorStandId));
            if (armorStand != null) armorStand.remove();
        }

        // Удаляем баннер
        String[] locData = territoryData.getString("flags." + clanName + "." + flagId + ".location").split(",");
        if (locData.length == 3) {
            World world = Bukkit.getWorld(territoryData.getString("flags." + clanName + "." + flagId + ".world"));
            Location loc = new Location(
                    world,
                    Integer.parseInt(locData[0]),
                    Integer.parseInt(locData[1]),
                    Integer.parseInt(locData[2])
            );
            loc.getBlock().setType(Material.AIR);
        }
    }

    private boolean isAdjacent(int[] territory1, int minX, int minZ, int maxX, int maxZ) {
        // Проверяем, что территории соприкасаются хотя бы одной стороной
        return (maxX == territory1[0] - 1 || minX == territory1[2] + 1 ||
                maxZ == territory1[1] - 1 || minZ == territory1[3] + 1);
    }

    /**
     * Удаляет флаг и пересчитывает территорию клана
     */
    public boolean removeClanFlag(Location flagLocation) {
        if (!territoryData.contains("flags")) {
            return false;
        }

        for (String clanName : territoryData.getConfigurationSection("flags").getKeys(false)) {
            ConfigurationSection clanFlags = territoryData.getConfigurationSection("flags." + clanName);
            for (String flagId : clanFlags.getKeys(false)) {
                String[] data = clanFlags.getString(flagId).split(",");
                if (data.length == 7) {
                    int x = Integer.parseInt(data[4]);
                    int y = Integer.parseInt(data[5]);
                    int z = Integer.parseInt(data[6]);

                    if (x == flagLocation.getBlockX() &&
                            y == flagLocation.getBlockY() &&
                            z == flagLocation.getBlockZ()) {

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

        // Добавляем основную территорию
        int[] mainTerritory = getClanTerritory(clanName);
        if (mainTerritory != null) {
            territories.add(mainTerritory);
        }

        // Добавляем территории флагов
        if (territoryData.contains("flags." + clanName)) {
            for (String flagId : territoryData.getConfigurationSection("flags." + clanName).getKeys(false)) {
                String[] coords = territoryData.getString("flags." + clanName + "." + flagId + ".territory").split(",");
                territories.add(new int[]{
                        Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]),
                        Integer.parseInt(coords[3])
                });
            }
        }

        return territories;
    }

}