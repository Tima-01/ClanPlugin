package org.plugin.clansPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TeleportManager {

    private final File file;
    private final FileConfiguration config;
    private final Map<String, Location> teleportPoints = new HashMap<>();

    public TeleportManager(File dataFolder) {
        this.file = new File(dataFolder, "teleports.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        loadTeleportPoints();
    }

    private void loadTeleportPoints() {
        for (String clan : config.getKeys(false)) {
            String world = config.getString(clan + ".world");
            double x = config.getDouble(clan + ".x");
            double y = config.getDouble(clan + ".y");
            double z = config.getDouble(clan + ".z");
            float yaw = (float) config.getDouble(clan + ".yaw");
            float pitch = (float) config.getDouble(clan + ".pitch");

            if (Bukkit.getWorld(world) != null) {
                Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                teleportPoints.put(clan, location);
            }
        }
    }

    public void setTeleportPoint(String clanName, Location location) {
        teleportPoints.put(clanName, location);
        config.set(clanName + ".world", location.getWorld().getName());
        config.set(clanName + ".x", location.getX());
        config.set(clanName + ".y", location.getY());
        config.set(clanName + ".z", location.getZ());
        config.set(clanName + ".yaw", location.getYaw());
        config.set(clanName + ".pitch", location.getPitch());
        save();
    }

    public Location getTeleportPoint(String clanName) {
        return teleportPoints.get(clanName);
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
