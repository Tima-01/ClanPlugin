//package org.plugin.clansPlugin.managers;
//
//import de.bluecolored.bluemap.api.BlueMapAPI;
//import de.bluecolored.bluemap.api.markers.MarkerSet;
//import de.bluecolored.bluemap.api.markers.ShapeMarker;
//import de.bluecolored.bluemap.api.math.Vector2d;
//import org.bukkit.configuration.ConfigurationSection;
//import org.plugin.clansPlugin.managers.TerritoryManager;
//
//import java.awt.Color;
//import java.util.ArrayList;
//import java.util.List;
//
//public class BlueMapManager {
//
//    private final TerritoryManager territoryManager;
//
//    public BlueMapManager(TerritoryManager territoryManager) {
//        this.territoryManager = territoryManager;
//    }
//
//    public void updateClanTerritories() {
//        BlueMapAPI api = BlueMapAPI.getInstance();
//        if (api == null) return;
//
//        MarkerSet markerSet = api.getMarkerAPI().getMarkerSet("clans");
//        if (markerSet == null) {
//            markerSet = api.getMarkerAPI().createMarkerSet("clans", "Clan Territories");
//        }
//
//        markerSet.clearMarkers();
//
//        ConfigurationSection sec = territoryManager.getTerritoryData().getConfigurationSection("territories");
//        if (sec == null) return;
//
//        for (String clanName : sec.getKeys(false)) {
//            List<String> chunks = territoryManager.getClanChunks(clanName);
//
//            List<Vector2i> points = new ArrayList<>();
//
//            for (String chunkStr : chunks) {
//                String[] parts = chunkStr.split(",");
//                int chunkX = Integer.parseInt(parts[0]);
//                int chunkZ = Integer.parseInt(parts[1]);
//
//                points.add(new Vector2i(chunkX * 16, chunkZ * 16));
//                points.add(new Vector2i(chunkX * 16 + 16, chunkZ * 16));
//                points.add(new Vector2i(chunkX * 16 + 16, chunkZ * 16 + 16));
//                points.add(new Vector2i(chunkX * 16, chunkZ * 16 + 16));
//            }
//
//            ShapeMarker shapeMarker = markerSet.createShapeMarker(
//                    "clan_" + clanName,
//                    clanName,
//                    points,
//                    false
//            );
//
//            shapeMarker.setColor(new Color(255, 0, 0, 128));
//        }
//
//        api.getMarkerAPI().save();
//    }
//}
