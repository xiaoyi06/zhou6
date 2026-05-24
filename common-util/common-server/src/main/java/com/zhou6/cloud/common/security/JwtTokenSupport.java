package com.zhou6.cloud.common.security;

import com.zhou6.cloud.common.handler.TokenException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtTokenSupport {

    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final byte[] secret;

    public JwtTokenSupport(String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String createToken(String userId, String sessionId, long ttlSeconds) {
        long now = Instant.now().getEpochSecond();
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"" + escape(userId) + "\",\"sid\":\"" + escape(sessionId)
                + "\",\"iat\":" + now + ",\"exp\":" + (now + ttlSeconds) + "}";
        String unsignedToken = encode(header) + "." + encode(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtClaims verifyAndGetClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new TokenException("Token 格式无效");
        }
        String unsignedToken = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            throw new TokenException("Token 签名无效");
        }
        String payload = decodePayload(parts[1]);
        long exp = readLong(payload, "exp");
        if (Instant.now().getEpochSecond() >= exp) {
            throw new TokenException("Token 已过期");
        }
        return new JwtClaims(readString(payload, "sub"), readString(payload, "sid"));
    }

    private String encode(String value) {
        return URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("JWT 签名失败", ex);
        }
    }

    private String decodePayload(String payload) {
        try {
            return new String(URL_DECODER.decode(payload), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new TokenException("Token 内容无效", ex);
        }
    }

    private long readLong(String payload, String key) {
        String marker = "\"" + key + "\":";
        int start = payload.indexOf(marker);
        if (start < 0) {
            throw new TokenException("Token 缺少字段：" + key);
        }
        start += marker.length();
        int end = start;
        while (end < payload.length() && Character.isDigit(payload.charAt(end))) {
            end++;
        }
        try {
            return Long.parseLong(payload.substring(start, end));
        } catch (NumberFormatException ex) {
            throw new TokenException("Token 字段无效：" + key, ex);
        }
    }

    private String readString(String payload, String key) {
        String marker = "\"" + key + "\":\"";
        int start = payload.indexOf(marker);
        if (start < 0) {
            throw new TokenException("Token 缺少字段：" + key);
        }
        start += marker.length();
        int end = payload.indexOf('"', start);
        if (end < 0) {
            throw new TokenException("Token 字段无效：" + key);
        }
        return payload.substring(start, end);
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
