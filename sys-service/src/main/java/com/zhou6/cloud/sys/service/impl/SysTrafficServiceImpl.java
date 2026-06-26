package com.zhou6.cloud.sys.service.impl;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class SysTrafficServiceImpl extends BaseSysService implements SysTrafficService {

    private static final Logger log = LoggerFactory.getLogger(SysTrafficServiceImpl.class);
    private static final String FLUSH_LOCK_KEY = "lock:sys:traffic:flush";
    private static final Duration FLUSH_LOCK_TTL = Duration.ofMinutes(2);
    private static final Duration METRIC_TTL = Duration.ofHours(2);
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);
    private static final DateTimeFormatter BUCKET_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter();

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
            redisTemplate.expire(pvKey, METRIC_TTL);
            redisTemplate.expire(uvKey, METRIC_TTL);
            redisTemplate.expire(rtKey, METRIC_TTL);
        } catch (Exception ex) {
            log.warn("Record traffic metric failed, skip current metric", ex);
        }
    }

    @Override
    public synchronized void flush() {
        String lockValue = UUID.randomUUID().toString();
        if (!tryLock(FLUSH_LOCK_KEY, lockValue, FLUSH_LOCK_TTL)) {
            return;
        }
        try {
            Set<String> keys = scanKeys(SysRedisKeys.TRAFFIC_PV_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }
            for (String pvKey : keys) {
                if (pvKey.contains(":processing:")) {
                    continue;
                }
                TrafficKey key = parseTrafficKey(pvKey);
                if (key == null) {
                    continue;
                }
                String processingPvKey = moveKeyToProcessing(pvKey);
                if (processingPvKey == null) {
                    continue;
                }
                String uvKey = SysRedisKeys.TRAFFIC_UV_PREFIX + key.bucket() + ":" + key.route();
                String rtKey = SysRedisKeys.TRAFFIC_RT_PREFIX + key.bucket() + ":" + key.route();
                String processingUvKey = moveKeyToProcessing(uvKey);
                String processingRtKey = moveKeyToProcessing(rtKey);
                long pv = numberValue(redisTemplate.opsForValue().get(processingPvKey));
                Long uvSize = processingUvKey == null ? null : redisTemplate.opsForSet().size(processingUvKey);
                long uv = uvSize == null ? 0 : uvSize;
                long rt = numberValue(processingRtKey == null ? null : redisTemplate.opsForValue().get(processingRtKey));
                int avgRt = pv == 0 ? 0 : Math.toIntExact(rt / pv);
                trafficStatMapper.upsert(key.route(), pv, uv, avgRt,
                        Timestamp.valueOf(LocalDateTime.parse(key.bucket(), BUCKET_FORMATTER)));
                deleteProcessingKeys(processingPvKey, processingUvKey, processingRtKey);
            }
        } catch (Exception ex) {
            log.warn("Flush traffic stats failed", ex);
        } finally {
            unlock(FLUSH_LOCK_KEY, lockValue);
        }
    }

    @Override
    public void sync() {
        flush();
    }

    @Override
    public synchronized void clear() {
        try {
            deleteKeys(SysRedisKeys.TRAFFIC_PV_PREFIX);
            deleteKeys(SysRedisKeys.TRAFFIC_UV_PREFIX);
            deleteKeys(SysRedisKeys.TRAFFIC_RT_PREFIX);
            trafficStatMapper.clearAll();
        } catch (Exception ex) {
            log.warn("Clear traffic stats failed", ex);
            throw new IllegalStateException("清空流量统计失败", ex);
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
        // 时间桶格式为 yyyy-MM-ddTHH:mm，本身包含冒号；分隔符取最后一个冒号，
        // 避免将分钟部分（如 "00"）错误拼入接口路径。
        int index = body.lastIndexOf(':');
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

    private void deleteKeys(String prefix) {
        Set<String> keys = scanKeys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(1000).build())) {
                while (cursor.hasNext()) {
                    String key = redisTemplate.getStringSerializer().deserialize(cursor.next());
                    if (key != null) {
                        keys.add(key);
                    }
                }
            }
            return keys;
        });
    }

    private boolean tryLock(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, ttl));
    }

    private void unlock(String key, String value) {
        redisTemplate.execute(UNLOCK_SCRIPT, List.of(key), value);
    }

    private String moveKeyToProcessing(String key) {
        String processingKey = key + ":processing:" + UUID.randomUUID();
        try {
            redisTemplate.rename(key, processingKey);
            redisTemplate.expire(processingKey, FLUSH_LOCK_TTL);
            return processingKey;
        } catch (Exception ex) {
            return null;
        }
    }

    private void deleteProcessingKeys(String... keys) {
        List<String> existingKeys = new ArrayList<>();
        for (String key : keys) {
            if (key != null) {
                existingKeys.add(key);
            }
        }
        if (!existingKeys.isEmpty()) {
            redisTemplate.delete(existingKeys);
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
