package me.evisual.authenticator.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OtpAuthUriBuilder
{
    private final String issuer;
    public OtpAuthUriBuilder(String issuer) { this.issuer = issuer; }

    /**
     * Builds the standard otpauth:// URI for provisioning.
     */
    public String buildOtpAuthUri(String secret, String accountName) {
        String issuerEnc  = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String accountEnc = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuerEnc, accountEnc, secret, issuerEnc
        );
    }
}
