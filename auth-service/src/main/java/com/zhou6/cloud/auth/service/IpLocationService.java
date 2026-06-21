package com.zhou6.cloud.auth.service;

import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 调用外部 IP 归属地服务，将客户端公网 IP 转换为审计展示地点。
 */
@Service
public class IpLocationService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String endpoint;

    public IpLocationService(ObjectMapper objectMapper,
            @Value("${zhou6.auth.ip-location.endpoint:https://ipwho.is/{ip}}") String endpoint) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(1));
        requestFactory.setReadTimeout(Duration.ofSeconds(1));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
        this.endpoint = endpoint;
    }

    public String resolve(String ipAddress) {
        if (!hasText(ipAddress)) {
            return "未知";
        }
        if (isPrivateAddress(ipAddress)) {
            return "内网地址";
        }
        try {
            String url = UriComponentsBuilder.fromUriString(endpoint).buildAndExpand(ipAddress).toUriString();
            String body = restClient.get().uri(url).retrieve().body(String.class);
            JsonNode response = objectMapper.readTree(body);
            if (!response.path("success").asBoolean(false)) {
                return "未知";
            }
            return join(response.path("country").asText(), response.path("region").asText(),
                    response.path("city").asText());
        } catch (Exception ignored) {
            return "未知";
        }
    }

    private boolean isPrivateAddress(String ipAddress) {
        String ip = ipAddress.trim();
        if ("localhost".equalsIgnoreCase(ip) || "::1".equals(ip) || ip.startsWith("127.")
                || ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (!ip.startsWith("172.")) {
            return false;
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String join(String country, String region, String city) {
        StringBuilder location = new StringBuilder();
        append(location, country);
        append(location, region);
        append(location, city);
        return location.isEmpty() ? "未知" : location.toString();
    }

    private void append(StringBuilder target, String value) {
        if (hasText(value) && !"null".equalsIgnoreCase(value)) {
            target.append(value.trim());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
