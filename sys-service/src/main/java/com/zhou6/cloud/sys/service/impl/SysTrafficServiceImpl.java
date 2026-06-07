package com.zhou6.cloud.sys.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import com.zhou6.cloud.sys.constant.SysRedisKeys;
import com.zhou6.cloud.sys.dto.TrafficQueryDTO;
import com.zhou6.cloud.sys.entity.SysTrafficStat;
import com.zhou6.cloud.sys.mapper.SysTrafficStatMapper;
import com.zhou6.cloud.sys.service.SysCacheService;
import com.zhou6.cloud.sys.service.SysTrafficService;
import com.zhou6.cloud.sys.vo.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SysTrafficServiceImpl extends BaseSysService implements SysTrafficService {

    private static final Logger log = LoggerFactory.getLogger(SysTrafficServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final SysTrafficStatMapper trafficStatMapper;
    private final SysCacheService cacheService;

    public SysTrafficServiceImpl(StringRedisTemplate redisTemplate,
            SysTrafficStatMapper trafficStatMapper, SysCacheService cacheService) {
        this.redisTemplate = redisTemplate;
        this.trafficStatMapper = trafficStatMapper;
        this.cacheService = cacheService;
    }

    @Override
    public void record(HttpServletRequest request, long durationMs) {
        if (!cacheService.isMaintenanceMode()) {
            try {
                String monitor = redisTemplate.opsForValue().get(SysRedisKeys.CONFIG_PREFIX + "sys.traffic.monitor");
                if ("false".equalsIgnoreCase(stripJsonString(monitor))) {
                    return;
                }
            } catch (Exception ex) {
                log.warn("Read traffic switch failed, skip current metric", ex);
                return;
            }
        }
        try {
            String route = normalizeRoute(request.getRequestURI());
            String bucket = currentBucket();
            String pvKey = SysRedisKeys.TRAFFIC_PV_PREFIX + bucket + ":" + route;
            String uvKey = SysRedisKeys.TRAFFIC_UV_PREFIX + bucket + ":" + route;
            String rtKey = SysRedisKeys.TRAFFIC_RT_PREFIX + bucket + ":" + route;
            redisTemplate.opsForValue().increment(pvKey);
            redisTemplate.opsForSet().add(uvKey, resolveVisitor(request));
            redisTemplate.opsForValue().increment(rtKey, durationMs);
        } catch (Exception ex) {
            log.warn("Record traffic metric failed, skip current metric", ex);
        }
    }

    @Override
    public void flush() {
        try {
            Set<String> keys = redisTemplate.keys(SysRedisKeys.TRAFFIC_PV_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }
            for (String pvKey : keys) {
                TrafficKey key = parseTrafficKey(pvKey);
                if (key == null) {
                    continue;
                }
                String uvKey = SysRedisKeys.TRAFFIC_UV_PREFIX + key.bucket() + ":" + key.route();
                String rtKey = SysRedisKeys.TRAFFIC_RT_PREFIX + key.bucket() + ":" + key.route();
                long pv = numberValue(redisTemplate.opsForValue().get(pvKey));
                long uv = redisTemplate.opsForSet().size(uvKey) == null ? 0 : redisTemplate.opsForSet().size(uvKey);
                long rt = numberValue(redisTemplate.opsForValue().get(rtKey));
                int avgRt = pv == 0 ? 0 : Math.toIntExact(rt / pv);
                trafficStatMapper.upsert(key.route(), pv, uv, avgRt,
                        Timestamp.valueOf(LocalDateTime.parse(key.bucket())));
                redisTemplate.delete(List.of(pvKey, uvKey, rtKey));
            }
        } catch (Exception ex) {
            log.warn("Flush traffic stats failed", ex);
        }
    }

    @Override
    public PageResponse<SysTrafficStat> page(TrafficQueryDTO dto) {
        TrafficQueryDTO query = dto == null ? new TrafficQueryDTO() : dto;
        long pageNum = pageNum(query.getPageNum());
        long pageSize = pageSize(query.getPageSize());
        String apiRoute = hasText(query.getApiRoute()) ? query.getApiRoute() : null;
        return new PageResponse<>(trafficStatMapper.countPage(apiRoute), pageNum, pageSize,
                trafficStatMapper.selectPage(apiRoute, pageSize, (pageNum - 1) * pageSize));
    }

    private String currentBucket() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        return now.withMinute((now.getMinute() / 5) * 5).toString();
    }

    private TrafficKey parseTrafficKey(String key) {
        String body = key.substring(SysRedisKeys.TRAFFIC_PV_PREFIX.length());
        int index = body.indexOf(':');
        if (index < 0) {
            return null;
        }
        return new TrafficKey(body.substring(0, index), body.substring(index + 1));
    }

    private long numberValue(String value) {
        try {
            return value == null ? 0 : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private String normalizeRoute(String uri) {
        return uri == null || uri.isBlank() ? "/" : uri;
    }

    private String resolveVisitor(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (hasText(userId)) {
            return "user:" + userId;
        }
        return "ip:" + request.getRemoteAddr();
    }

    private String stripJsonString(String value) {
        if (value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private record TrafficKey(String bucket, String route) {
    }
}
