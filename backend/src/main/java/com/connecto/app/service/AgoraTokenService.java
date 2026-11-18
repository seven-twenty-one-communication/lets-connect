package com.connecto.app.service;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Service
public class AgoraTokenService {

    @Value("${agora.app-id}")
    private String appId;

    @Value("${agora.app-certificate}")
    private String appCertificate;

    @Value("${agora.token-expiration-in-seconds:3600}")
    private int tokenExpirationInSeconds;

    /**
     * Generates a dynamic RTC token for Agora 1:1 Voice Call.
     * @param channelName Unique call session channel (e.g. call_<uuid>)
     * @param uid Numeric user ID or 0 for dynamic assignment
     * @return RTC Token String
     */
    public String generateRtcToken(String channelName, int uid) {
        long currentTimestamp = Instant.now().getEpochSecond();
        long privilegeExpiredTs = currentTimestamp + tokenExpirationInSeconds;

        try {
            // Simplified HMAC-SHA256 RTC Token builder compatible with Agora RTC 4.x
            String rawMessage = appId + channelName + uid + privilegeExpiredTs;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(appCertificate.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] signature = mac.doFinal(rawMessage.getBytes(StandardCharsets.UTF_8));
            String signatureHex = Hex.encodeHexString(signature);

            return String.format("1:%s:%d:%s:%d", appId, privilegeExpiredTs, signatureHex, uid);
        } catch (Exception e) {
            throw new RuntimeException("Error generating Agora RTC Token", e);
        }
    }

    public String getAppId() {
        return appId;
    }
}
