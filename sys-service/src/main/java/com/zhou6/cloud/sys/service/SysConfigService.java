package com.zhou6.cloud.sys.service;

import com.zhou6.cloud.sys.dto.ConfigDTO;
import com.zhou6.cloud.sys.dto.ConfigQueryDTO;
import com.zhou6.cloud.sys.entity.SysConfig;
import com.zhou6.cloud.sys.vo.PageResponse;

public interface SysConfigService {

    PageResponse<SysConfig> page(ConfigQueryDTO dto);

    void add(ConfigDTO dto);

    void edit(ConfigDTO dto);

    void delete(String id);

    void refreshCache(String key);
}
