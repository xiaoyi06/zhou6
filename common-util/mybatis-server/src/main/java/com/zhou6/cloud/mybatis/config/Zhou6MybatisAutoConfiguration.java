package com.zhou6.cloud.mybatis.config;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(Zhou6IdProperties.class)
public class Zhou6MybatisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdentifierGenerator.class)
    public IdentifierGenerator identifierGenerator(Zhou6IdProperties properties) {
        validateRange("worker-id", properties.getWorkerId());
        validateRange("datacenter-id", properties.getDatacenterId());
        // 所有引入 mybatis-server 的服务统一使用这份雪花 ID 生成器。
        return new DefaultIdentifierGenerator(properties.getWorkerId(), properties.getDatacenterId());
    }

    private void validateRange(String name, long value) {
        if (value < 0 || value > 31) {
            throw new IllegalArgumentException("zhou6.id." + name + " 必须在 0 到 31 之间");
        }
    }
}
