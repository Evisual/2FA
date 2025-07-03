package me.evisual.authenticator.security;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.binary.Base32;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class TwoFactorAuthUtil
{
    private final String ISSUER;

    public TwoFactorAuthUtil(String issuer)
    {
        this.ISSUER = issuer;
    }

    /**
     * Generates a new random secret key, Base32-encoded.
     */
    public String generateSecret() throws NoSuchAlgorithmException {
        // TOTP default is HmacSHA1, 160-bit keys
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
        keyGen.init(160);
        SecretKey key = keyGen.generateKey();

        Base32 base32 = new Base32();
        return base32.encodeToString(key.getEncoded())
                .replace("=", ""); // drop padding for URI cleanliness
    }

    /**
     * Builds the standard otpauth:// URI for provisioning.
     */
    public String buildOtpAuthUri(String secret, String accountName) {
        // URL-encode issuer and account name
        String issuerEnc    = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8);
        String accountEnc   = URLEncoder.encode(accountName, StandardCharsets.UTF_8);

        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuerEnc, accountEnc, secret, issuerEnc
        );
    }

    /**
     * Renders a QR code for that URI and returns a Base64 data URI to be embeded in an <IMG>
     */
    public String generateQrCodeDataUri(String otpAuthUri, int size) throws WriterException, IOException {
        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix matrix = qrWriter.encode(otpAuthUri, BarcodeFormat.QR_CODE, size, size);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // write out as PNG
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);

        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        return "data:image/png;base64," + base64;
    }

    /**
     * Verifies a user-entered code against the secret, allowing one step of clock drift.
     */
    public boolean verifyCode(String base32Secret, String code)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Base32 base32 = new Base32();
        byte[] keyBytes = base32.decode(base32Secret);

        TimeBasedOneTimePasswordGenerator totp =
                new TimeBasedOneTimePasswordGenerator();

        // Wrap raw bytes in SecretKeySpec using the TOTP algorithm
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, totp.getAlgorithm());

        Instant now = Instant.now();
        // check current timestep
        int generated = totp.generateOneTimePassword(signingKey, now);
        if (String.format("%06d", generated).equals(code)) {
            return true;
        }

        // allow ±1 step to account for slight clock skew
        Instant prev = now.minusSeconds(30);
        Instant next = now.plusSeconds(30);

        return String.format("%06d", totp.generateOneTimePassword(signingKey, prev)).equals(code)
                || String.format("%06d", totp.generateOneTimePassword(signingKey, next)).equals(code);
    }
}
