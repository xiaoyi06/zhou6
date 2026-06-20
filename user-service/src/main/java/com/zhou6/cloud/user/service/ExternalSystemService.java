package com.zhou6.cloud.user.service;

import java.util.List;

import com.zhou6.cloud.user.dto.ExternalSystemChangeStatusDTO;
import com.zhou6.cloud.user.dto.ExternalSystemIdDTO;
import com.zhou6.cloud.user.dto.ExternalSystemQueryDTO;
import com.zhou6.cloud.user.dto.ExternalSystemSaveDTO;
import com.zhou6.cloud.user.vo.ExternalSystemVO;
import com.zhou6.cloud.user.vo.PageResponse;

/** 外部系统管理业务接口。 */
public interface ExternalSystemService {

    PageResponse<ExternalSystemVO> page(ExternalSystemQueryDTO dto);
    void add(ExternalSystemSaveDTO dto);
    void edit(ExternalSystemSaveDTO dto);
    void delete(ExternalSystemIdDTO dto);
    void changeStatus(ExternalSystemChangeStatusDTO dto);
    /** 仅返回启用的系统，供角色所属系统下拉框使用。 */
    List<ExternalSystemVO> listEnabled();
}
