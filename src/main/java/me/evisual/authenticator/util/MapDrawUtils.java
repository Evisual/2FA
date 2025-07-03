package me.evisual.authenticator.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class MapDrawUtils
{
    public static boolean giveDrawnMap(Player player, String dataUri)
    {
        String base64 = dataUri.substring(dataUri.indexOf(',') + 1);
        BufferedImage image;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new IOException("Decoded data is not a valid image");
            }
        } catch (IOException e) {
            // TODO: Throw an error
            return false;
        }

        // Create a new map view and apply our custom renderer
        World world = player.getWorld();
        MapView map = Bukkit.createMap(world);
        map.getRenderers().clear();
        map.addRenderer(new ImageMapRenderer(image));

        // Give the filled map item to the player
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapId(map.getId());
        mapItem.setItemMeta(meta);

        player.getInventory().addItem(mapItem);
        return true;
    }

    // Inner class to render the image onto the map canvas
    private static class ImageMapRenderer extends MapRenderer {
        private final BufferedImage image;
        private boolean rendered = false;

        public ImageMapRenderer(BufferedImage image) {
            super(true);
            this.image = image;
        }

        @Override
        public void render(MapView view, MapCanvas canvas, Player player) {
            if (rendered) {
                return;
            }

            // Map resolution is always 128x128
            final int width = 128;
            final int height = 128;

            // Scale image to map resolution
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int rgb = image.getRGB(x * image.getWidth() / width,
                            y * image.getHeight() / height);
                    java.awt.Color awtColor = new java.awt.Color(rgb, true);
                    byte mcColor = MapPalette.matchColor(awtColor);
                    canvas.setPixel(x, y, mcColor);
                }
            }
            rendered = true;
        }
    }
}
