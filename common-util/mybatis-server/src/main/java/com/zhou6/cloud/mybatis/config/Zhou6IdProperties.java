package com.zhou6.cloud.mybatis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zhou6.id")
public class Zhou6IdProperties {

    /**
     * 雪花算法工作机器 ID，取值范围 0-31。
     */
    private long workerId = 1L;

    /**
     * 雪花算法数据中心 ID，取值范围 0-31。
     */
    private long datacenterId = 1L;

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(long datacenterId) {
        this.datacenterId = datacenterId;
    }
}
