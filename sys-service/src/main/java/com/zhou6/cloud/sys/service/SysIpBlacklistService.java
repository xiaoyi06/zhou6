package com.zhou6.cloud.sys.service;

import java.util.List;

import com.zhou6.cloud.sys.dto.IpBlacklistDTO;
import com.zhou6.cloud.sys.dto.IpBlacklistQueryDTO;
import com.zhou6.cloud.sys.vo.IpBlacklistVO;
import com.zhou6.cloud.sys.vo.PageResponse;

public interface SysIpBlacklistService {

    PageResponse<IpBlacklistVO> page(IpBlacklistQueryDTO dto);

    void add(IpBlacklistDTO dto);

    void edit(IpBlacklistDTO dto);

    void delete(List<String> ids);
}
