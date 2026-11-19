package com.letsconnect.app.service;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Production Agora RTC AccessToken2 Generator.
 * Implements Agora's AccessToken2 specification for secure, production 1:1 audio channels.
 */
@Service
public class AgoraAccessToken2Service {

    @Value("${agora.app-id:}")
    private String appId;

    @Value("${agora.app-certificate:}")
    private String appCertificate;

    @Value("${agora.token-expiration-in-seconds:3600}")
    private int tokenExpirationInSeconds;

    @Value("${integrations.mock.voice:false}")
    private boolean mockVoice;

    // Agora AccessToken2 Service Types
    public static final short SERVICE_TYPE_RTC = 1;

    // Agora RTC Privileges
    public static final short PRIVILEGE_JOIN_CHANNEL = 1;
    public static final short PRIVILEGE_PUBLISH_AUDIO_STREAM = 2;

    /**
     * Builds a production-grade Agora AccessToken2.
     *
     * @param channelName Unique call session channel (e.g. call_session_xyz)
     * @param uid User ID (0 allows any subscriber to bind within the session)
     * @return Base64-encoded signed Agora AccessToken2
     */
    public String buildToken(String channelName, int uid) {
        if (mockVoice) {
            return "MOCK_AGORA_" + UUID.randomUUID();
        }
        if (appId.isBlank() || appCertificate.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Agora credentials are not configured");
        }

        int issueTs = (int) (System.currentTimeMillis() / 1000);
        int expireTs = issueTs + tokenExpirationInSeconds;
        int salt = (int) (Math.random() * 99999999);

        try {
            // 1. Pack RTC Service Privileges
            TreeMap<Short, Integer> privileges = new TreeMap<>();
            privileges.put(PRIVILEGE_JOIN_CHANNEL, expireTs);
            privileges.put(PRIVILEGE_PUBLISH_AUDIO_STREAM, expireTs);

            ByteArrayOutputStream serviceBytes = new ByteArrayOutputStream();
            DataOutputStream serviceStream = new DataOutputStream(serviceBytes);

            // Channel name
            byte[] channelBytes = channelName.getBytes(StandardCharsets.UTF_8);
            writeShortLE(serviceStream, (short) channelBytes.length);
            serviceStream.write(channelBytes);

            // UID
            serviceStream.writeInt(Integer.reverseBytes(uid));

            // Privileges map
            writeShortLE(serviceStream, (short) privileges.size());
            for (var entry : privileges.entrySet()) {
                writeShortLE(serviceStream, entry.getKey());
                serviceStream.writeInt(Integer.reverseBytes(entry.getValue()));
            }

            byte[] servicePayload = serviceBytes.toByteArray();

            // 2. Build Message to Sign
            ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
            DataOutputStream msgStream = new DataOutputStream(msgBytes);

            msgStream.write(appId.getBytes(StandardCharsets.UTF_8));
            msgStream.writeInt(Integer.reverseBytes(issueTs));
            msgStream.writeInt(Integer.reverseBytes(salt));
            writeShortLE(msgStream, SERVICE_TYPE_RTC);
            writeShortLE(msgStream, (short) servicePayload.length);
            msgStream.write(servicePayload);

            byte[] messageToSign = msgBytes.toByteArray();

            // 3. HMAC-SHA256 Signature
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appCertificate.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(messageToSign);

            // 4. Final Token Assembly
            ByteArrayOutputStream tokenBytes = new ByteArrayOutputStream();
            DataOutputStream tokenStream = new DataOutputStream(tokenBytes);

            writeShortLE(tokenStream, (short) signature.length);
            tokenStream.write(signature);
            tokenStream.write(messageToSign);

            String base64Token = Base64.getEncoder().encodeToString(tokenBytes.toByteArray());
            return "007" + base64Token; // "007" prefix identifies Agora AccessToken2
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Agora AccessToken2", e);
        }
    }

    private void writeShortLE(DataOutputStream stream, short val) throws Exception {
        stream.writeByte(val & 0xFF);
        stream.writeByte((val >> 8) & 0xFF);
    }

    public String getAppId() {
        return appId;
    }
}
