package com.zhou6.cloud.sys.filter;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.service.SysCacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 系统维护开关过滤器，Redis 异常由缓存服务降级放行。
 */
@Component
public class SystemControlFilter extends OncePerRequestFilter {

    private final SysCacheService cacheService;
    private final ObjectMapper objectMapper;

    public SystemControlFilter(SysCacheService cacheService, ObjectMapper objectMapper) {
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (shouldBlock(request)) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(R.fail(503, maintenanceMessage())));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldBlock(HttpServletRequest request) {
        if (!cacheService.isMaintenanceMode()) {
            return false;
        }
        String userId = request.getHeader("X-User-Id");
        return !cacheService.isWhitelisted("IP", resolveClientIp(request))
                && !cacheService.isWhitelisted("USER", userId)
                && !cacheService.isWhitelisted("ROUTE", request.getRequestURI());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String clientIp = request.getHeader("X-Client-Ip");
        if (clientIp != null && !clientIp.isBlank()) {
            return clientIp;
        }
        return request.getRemoteAddr();
    }

    private String maintenanceMessage() {
        String value = cacheService.getConfigValue("sys.maintenance.message");
        if (value == null || value.isBlank()) {
            return "系统维护中，请稍后再试";
        }
        try {
            return objectMapper.readValue(value, String.class);
        } catch (Exception ex) {
            return "系统维护中，请稍后再试";
        }
    }
}
