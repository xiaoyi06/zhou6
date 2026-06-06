package com.zhou6.cloud.sys.service;

import java.util.List;

import com.zhou6.cloud.sys.dto.DictDataDTO;
import com.zhou6.cloud.sys.dto.DictQueryDTO;
import com.zhou6.cloud.sys.dto.DictTypeDTO;
import com.zhou6.cloud.sys.entity.SysDictData;
import com.zhou6.cloud.sys.entity.SysDictType;
import com.zhou6.cloud.sys.vo.PageResponse;

public interface SysDictService {

    PageResponse<SysDictType> typePage(DictQueryDTO dto);

    List<SysDictData> dataList(DictQueryDTO dto);

    void addType(DictTypeDTO dto);

    void editType(DictTypeDTO dto);

    void deleteType(String id);

    void addData(DictDataDTO dto);

    void editData(DictDataDTO dto);

    void deleteData(String id);
}
