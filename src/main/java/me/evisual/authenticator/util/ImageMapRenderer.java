package me.evisual.authenticator.util;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class ImageMapRenderer extends MapRenderer {

    private final BufferedImage image;
    private boolean rendered;

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

                // Draw map
                java.awt.Color awtColor = new java.awt.Color(rgb, true);
                byte mcColor = MapPalette.matchColor(awtColor);
                canvas.setPixel(x, y, mcColor);
            }
        }
        rendered = true;
    }
}