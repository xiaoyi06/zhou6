package com.zhou6.cloud.sys.service;

import java.util.List;

import com.zhou6.cloud.sys.dto.MenuUsageQueryDTO;
import com.zhou6.cloud.sys.dto.MenuUsageSummaryQueryDTO;
import com.zhou6.cloud.sys.dto.MenuUsageUserQueryDTO;
import com.zhou6.cloud.sys.vo.ApiUsageStatVO;
import com.zhou6.cloud.sys.vo.MenuUsageSummaryVO;
import com.zhou6.cloud.sys.vo.MenuUsageUserVO;
import com.zhou6.cloud.sys.vo.PageResponse;

public interface SysApiUsageService {

    void flush();

    /** 立即将 Redis 中待落库的菜单访问统计同步到数据库。 */
    void sync();

    /** 清空已落库和待落库的全部菜单访问统计。 */
    void clear();

    List<ApiUsageStatVO> frequent(MenuUsageQueryDTO dto);

    PageResponse<MenuUsageSummaryVO> menuPage(MenuUsageSummaryQueryDTO dto);

    PageResponse<MenuUsageUserVO> menuUserPage(MenuUsageUserQueryDTO dto);
}
