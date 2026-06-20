package com.zhou6.cloud.sys.service;

import java.util.List;

import com.zhou6.cloud.sys.dto.DictDataDTO;
import com.zhou6.cloud.sys.dto.DictQueryDTO;
import com.zhou6.cloud.sys.dto.DictTypeDTO;
import com.zhou6.cloud.sys.vo.DictDataVO;
import com.zhou6.cloud.sys.vo.DictTypeVO;
import com.zhou6.cloud.sys.vo.PageResponse;

public interface SysDictService {

    PageResponse<DictTypeVO> typePage(DictQueryDTO dto);

    List<DictDataVO> dataList(DictQueryDTO dto);

    void addType(DictTypeDTO dto);

    void editType(DictTypeDTO dto);

    void deleteType(String id);

    void addData(DictDataDTO dto);

    void editData(DictDataDTO dto);

    void deleteData(String id);
}
