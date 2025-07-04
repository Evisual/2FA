package me.evisual.authenticator.security;

import me.evisual.authenticator.util.SimpleTOTP;

public class TotpVerifier
{
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
