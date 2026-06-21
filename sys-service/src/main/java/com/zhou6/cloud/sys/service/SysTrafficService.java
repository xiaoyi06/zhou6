package com.zhou6.cloud.sys.service;

import com.zhou6.cloud.sys.dto.TrafficQueryDTO;
import com.zhou6.cloud.sys.entity.SysTrafficStat;
import com.zhou6.cloud.sys.vo.PageResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface SysTrafficService {

    void record(HttpServletRequest request, long durationMs);

    void flush();

    /** 立即将 Redis 中待落库的流量统计同步到数据库。 */
    void sync();

    /** 清空已落库和待落库的全部流量统计。 */
    void clear();

    PageResponse<SysTrafficStat> page(TrafficQueryDTO dto);
}
