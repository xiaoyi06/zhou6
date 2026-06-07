package com.zhou6.cloud.sys.service;

import java.util.List;

import com.zhou6.cloud.sys.dto.MenuUsageQueryDTO;
import com.zhou6.cloud.sys.entity.SysApiUsageStat;

public interface SysApiUsageService {

    void flush();

    List<SysApiUsageStat> frequent(MenuUsageQueryDTO dto);
}
