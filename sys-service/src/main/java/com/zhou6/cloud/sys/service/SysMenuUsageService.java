package com.zhou6.cloud.sys.service;

import java.util.List;

import com.zhou6.cloud.sys.dto.MenuUsageDTO;
import com.zhou6.cloud.sys.dto.MenuUsageQueryDTO;
import com.zhou6.cloud.sys.entity.SysMenuUsageStat;

public interface SysMenuUsageService {

    void record(MenuUsageDTO dto);

    void flush();

    List<SysMenuUsageStat> frequent(MenuUsageQueryDTO dto);
}
