package me.evisual.authenticator.security;

import me.evisual.authenticator.util.QrCode;
import me.evisual.authenticator.util.SimpleTOTP;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TwoFactorAuthUtil {
    private final String ISSUER;

    public TwoFactorAuthUtil(String issuer) {
        this.ISSUER = issuer;
    }

    /**
     * Generates a new random secret key, Base32-encoded.
     */
    public String generateSecret() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
        keyGen.init(160);
        SecretKey key = keyGen.generateKey();

        Base32 base32 = new Base32();
        // strip padding for URI cleanliness
        return base32.encodeToString(key.getEncoded()).replace("=", "");
    }

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

    /**
     * Builds the standard otpauth:// URI for provisioning.
     */
    public String buildOtpAuthUri(String secret, String accountName) {
        String issuerEnc  = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8);
        String accountEnc = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuerEnc, accountEnc, secret, issuerEnc
        );
    }

    /**
     * Verifies the TOTP code using a simple built-in implementation.
     */
    public boolean verifyCode(String base32Secret, String code) {
        String generated;
        try {
            generated = SimpleTOTP.generate(base32Secret);
        } catch (Exception e) {
            throw new RuntimeException("Error generating TOTP: " + e.getMessage(), e);
        }
        return generated.equals(code.trim());
    }
}

