package me.evisual.authenticator.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class MapItemFactory
{
    /**
     * Creates a filled‐map ItemStack whose MapView has a single ImageMapRenderer.
     * When the player holds or views the map, the map renderer will draw the image.
     */
    public ItemStack createMapItem(World world, BufferedImage img) {
        // Create a new MapView in the given world
        MapView map = Bukkit.createMap(world);

        // Remove all default renderers and add image renderer
        map.getRenderers().clear();
        map.addRenderer(new ImageMapRenderer(img));

        // Build the ItemStack for the filled map and assign the map ID
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapId(map.getId());
        mapItem.setItemMeta(meta);

        return mapItem;
    }
}
