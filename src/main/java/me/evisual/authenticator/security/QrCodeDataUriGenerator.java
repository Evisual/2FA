package me.evisual.authenticator.security;

import me.evisual.authenticator.util.nayuki.QrCode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class QrCodeDataUriGenerator
{
    /**
     * Generates a QR code image for the given provisioning URI, encodes it as PNG,
     * and returns a data URI ("data:image/png;base64,...").
     * @param otpAuthUri the otpauth:// URI to encode
     * @param scale      pixel size per QR module (e.g. 4)
     */
    public String generateQrCodeDataUri(String otpAuthUri, int scale) {
        // Encode text into QR matrix
        QrCode qr = QrCode.encodeText(otpAuthUri, QrCode.Ecc.MEDIUM);
        int border = 4;
        int size   = qr.size + border * 2;
        int pxSize = size * scale;

        // Draw to BufferedImage
        BufferedImage img = new BufferedImage(pxSize, pxSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, pxSize, pxSize);
        g.setColor(Color.BLACK);
        for (int y = 0; y < qr.size; y++) {
            for (int x = 0; x < qr.size; x++) {
                if (qr.getModule(x, y)) {
                    g.fillRect((x + border) * scale, (y + border) * scale, scale, scale);
                }
            }
        }
        g.dispose();

        // Encode image as Base64 data URI
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(img, "PNG", baos);
            String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + b64;
        } catch (IOException e) {
            throw new RuntimeException("QR code generation failed", e);
        }
    }
}
