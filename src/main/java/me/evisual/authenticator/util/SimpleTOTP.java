package me.evisual.authenticator.util;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import java.nio.ByteBuffer;
import java.time.Instant;

public class SimpleTOTP {
    private static final int    DIGITS   = 6;
    private static final String HMAC_ALGO = "HmacSHA1";
    private static final long   STEP     = 30L; // seconds

    public static String generate(String base32Secret) throws Exception {
        // 1. Decode Base32 secret to raw bytes
        Base32 codec = new Base32();
        byte[] key = codec.decode(base32Secret);

        // 2. Compute time‐step counter
        long counter = Instant.now().getEpochSecond() / STEP;
        byte[] data = ByteBuffer.allocate(8).putLong(counter).array();

        // 3. HMAC‐SHA1 of the counter
        Mac hmac = Mac.getInstance(HMAC_ALGO);
        hmac.init(new SecretKeySpec(key, HMAC_ALGO));
        byte[] hash = hmac.doFinal(data);

        // 4. Dynamic truncation to a 4-byte string
        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                |  (hash[offset + 3] & 0xFF);

        // 5. Truncate to the requested number of digits
        int otp = binary % (int) Math.pow(10, DIGITS);
        return String.format("%0" + DIGITS + "d", otp);
    }
}
