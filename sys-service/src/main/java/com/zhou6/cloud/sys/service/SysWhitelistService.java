package com.zhou6.cloud.sys.service;

import com.zhou6.cloud.sys.dto.WhitelistDTO;
import com.zhou6.cloud.sys.dto.WhitelistQueryDTO;
import com.zhou6.cloud.sys.entity.SysWhitelist;
import com.zhou6.cloud.sys.vo.PageResponse;

public interface SysWhitelistService {

    PageResponse<SysWhitelist> page(WhitelistQueryDTO dto);

    void add(WhitelistDTO dto);

    void edit(WhitelistDTO dto);

    void delete(String id);
}
