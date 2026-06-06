package com.zhou6.cloud.sys.service;

import com.zhou6.cloud.sys.dto.TrafficQueryDTO;
import com.zhou6.cloud.sys.entity.SysTrafficStat;
import com.zhou6.cloud.sys.vo.PageResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface SysTrafficService {

    void record(HttpServletRequest request, long durationMs);

    void flush();

    PageResponse<SysTrafficStat> page(TrafficQueryDTO dto);
}
