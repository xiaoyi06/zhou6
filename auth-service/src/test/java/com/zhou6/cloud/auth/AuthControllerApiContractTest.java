package com.zhou6.cloud.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;

import com.zhou6.cloud.auth.controller.AuthController;
import com.zhou6.cloud.auth.dto.LoginRequest;
import com.zhou6.cloud.auth.dto.RefreshRequest;
import com.zhou6.cloud.auth.vo.TokenResponse;
import com.zhou6.cloud.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerApiContractTest {

    private MockMvc mockMvc;

    private List<Endpoint> endpoints;

    @BeforeEach
    void setUp() {
        AuthService authService = mock(AuthService.class);
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token", "Bearer", 3600);
        when(authService.login(any(LoginRequest.class), eq("127.0.0.1"))).thenReturn(tokenResponse);
        when(authService.refresh(any(RefreshRequest.class), eq("127.0.0.1"))).thenReturn(tokenResponse);

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
        endpoints = List.of(
                new Endpoint("/auth/login", "{\"username\":\"zhou6_test\",\"password\":\"123456\"}"),
                new Endpoint("/auth/refresh", "{\"refreshToken\":\"refresh-token\"}"),
                new Endpoint("/auth/logout", "{\"refreshToken\":\"refresh-token\"}"));
    }

    @TestFactory
    Stream<DynamicTest> allAuthServiceEndpointsReturnSuccessContract() {
        return endpoints.stream()
                .map(endpoint -> DynamicTest.dynamicTest("POST " + endpoint.path(), () ->
                        mockMvc.perform(post(endpoint.path())
                                        .header("X-Client-Ip", "127.0.0.1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(endpoint.body()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value("000000"))));
    }

    private record Endpoint(String path, String body) {
    }
}
