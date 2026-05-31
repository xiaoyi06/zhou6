package com.zhou6.cloud.mybatis.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zhou6.cloud.mybatis.datapermission.DataPermissionInterceptor;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.plugin.Interceptor;
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

    /**
     * MyBatis-Plus 插件链：分页、数据权限等。
     */
    @Bean
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 注册数据权限 MyBatis 插件，只有标记 DataPermission 的 Mapper 方法才会追加数据范围条件。
     *
     * @return 数据权限拦截器
     */
    @Bean
    @ConditionalOnMissingBean(DataPermissionInterceptor.class)
    public Interceptor dataPermissionInterceptor() {
        return new DataPermissionInterceptor();
    }

    @Bean
    public ConfigurationCustomizer sqlDebugLogCustomizer() {
        return configuration -> configuration.setLogImpl(Slf4jImpl.class);
    }

    private void validateRange(String name, long value) {
        if (value < 0 || value > 31) {
            throw new IllegalArgumentException("zhou6.id." + name + " 必须在 0 到 31 之间");
        }
    }
}
