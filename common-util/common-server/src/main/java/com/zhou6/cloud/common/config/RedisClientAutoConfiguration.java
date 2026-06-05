package com.zhou6.cloud.common.config;

import java.time.Duration;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Redis 客户端公共配置。
 *
 * <p>通过 {@code spring.redis.enabled=true} 启用，可通过配置关闭。</p>
 */
@AutoConfiguration
@ConditionalOnClass({LettuceClientConfigurationBuilderCustomizer.class, ClientOptions.class})
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisClientAutoConfiguration {

    /**
     * 远程 Redis 连接容易被网络设备清理空闲 TCP 连接，开启 keepalive 和自动重连降低偶发断链影响。
     * 设置命令超时防止线程无限阻塞。
     */
    @Bean
    @ConditionalOnMissingBean(name = "zhou6LettuceClientConfigurationBuilderCustomizer")
    public LettuceClientConfigurationBuilderCustomizer zhou6LettuceClientConfigurationBuilderCustomizer() {
        return builder -> builder.clientOptions(ClientOptions.builder()
                .autoReconnect(true)
                .socketOptions(SocketOptions.builder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .keepAlive(true)
                        .build())
                .timeoutOptions(TimeoutOptions.builder()
                        .fixedTimeout(Duration.ofSeconds(3))
                        .build())
                .build());
    }
}
