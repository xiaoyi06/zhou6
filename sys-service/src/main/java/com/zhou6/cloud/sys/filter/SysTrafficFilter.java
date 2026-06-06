package com.zhou6.cloud.sys.filter;

import java.io.IOException;

import com.zhou6.cloud.sys.service.SysTrafficService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 系统服务流量采集过滤器，只写 Redis，不直接触碰数据库。
 */
@Component
public class SysTrafficFilter extends OncePerRequestFilter {

    private final SysTrafficService trafficService;

    public SysTrafficFilter(SysTrafficService trafficService) {
        this.trafficService = trafficService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            trafficService.record(request, System.currentTimeMillis() - startTime);
        }
    }
}
