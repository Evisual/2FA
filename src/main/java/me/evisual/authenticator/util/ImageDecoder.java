package me.evisual.authenticator.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageDecoder
{
    /**
     * Decodes a base64png data uri and returns
     * a BufferedImage
     */
    public BufferedImage decodeBase64Png(String dataUri) throws IOException
    {
        // Get the base64 string from uri
        String base64 = dataUri.substring(dataUri.indexOf(',') + 1);
        BufferedImage image;
        try {
            // Decode base64 into raw bytes
            byte[] bytes = Base64.getDecoder().decode(base64);
            image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new IOException("Decoded data is not a valid image");
            }
        } catch (IOException e) {
            // TODO: Throw an error
            return null;
        }

        return image;
    }
}
