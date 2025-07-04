package me.evisual.authenticator.security;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class SecretGenerator
{
    /**
     * Generates a new random secret key, Base32-encoded.
     */
    public String generateBase32Secret() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
        keyGen.init(160);
        SecretKey key = keyGen.generateKey();

        Base32 base32 = new Base32();
        // strip padding for URI cleanliness
        return base32.encodeToString(key.getEncoded()).replace("=", "");
    }
}
