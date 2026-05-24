package com.zhou6.cloud.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.security.JwtTokenSupport;
import com.zhou6.cloud.common.security.LoginSession;
import com.zhou6.cloud.user.dto.UserInfoResponse;
import com.zhou6.cloud.user.dto.VerifyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.url=jdbc:postgresql://81.70.186.59:5432/postgres?currentSchema=zhou6",
        "spring.datasource.username=postgres",
        "spring.datasource.password=a741741.",
        "spring.data.redis.host=81.70.186.59",
        "spring.data.redis.port=6379",
        "spring.data.redis.password=Redis@2026",
        "jwt.secret=zhou6-test-secret"
})
class UserInfoApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void verifyAndQueryUserInfo() {
        ResponseEntity<R<VerifyResponse>> verifyResponse = restTemplate.exchange(
                "http://localhost:" + port + "/userInfo/verify?username={username}&password={password}",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<>() {
                },
                Map.of("username", "zhou6_test", "password", "123456"));

        assertThat(verifyResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(verifyResponse.getBody()).isNotNull();
        assertThat(verifyResponse.getBody().getData()).isNotNull();
        assertThat(verifyResponse.getBody().getData().isVerified()).isTrue();

        VerifyResponse verifiedUser = verifyResponse.getBody().getData();
        String userId = verifiedUser.getUserId();
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        LoginSession loginSession = new LoginSession();
        loginSession.setUserId(userId);
        loginSession.setSessionId(sessionId);
        loginSession.setUsername(verifiedUser.getUsername());
        loginSession.setNickname(verifiedUser.getNickname());
        loginSession.setEmail(verifiedUser.getEmail());
        loginSession.setContactPhone(verifiedUser.getContactPhone());
        redisTemplate.opsForValue().set("zhou6:auth:session:" + userId,
                writeSession(loginSession), Duration.ofMinutes(1));

        String accessToken = new JwtTokenSupport("zhou6-test-secret")
                .createToken(userId, sessionId, 60);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer_" + accessToken);
        ResponseEntity<R<UserInfoResponse>> userInfoResponse = restTemplate.exchange(
                "http://localhost:" + port + "/userInfo/info",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                });

        assertThat(userInfoResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(userInfoResponse.getBody()).isNotNull();
        assertThat(userInfoResponse.getBody().getData()).isNotNull();
        assertThat(userInfoResponse.getBody().getData().getUsername()).isEqualTo("zhou6_test");
        assertThat(userInfoResponse.getBody().getData().getNickname()).isEqualTo("测试用户");
    }

    private String writeSession(LoginSession loginSession) {
        try {
            return objectMapper.writeValueAsString(loginSession);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
